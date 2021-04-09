package pt.unl.fct.di.apdc.avaliacaoindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.avaliacaoindividual.util.LogoutData;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {
	private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LogoutResource() {
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogout(LogoutData data) {
		LOG.fine("Logout attempt");
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.tokenID);
		Entity token = datastore.get(tokenKey);
		if (token != null) {
			long tokenEnd = token.getLong("token_end");
			if (tokenEnd < System.currentTimeMillis()) {
				return Response.status(Status.FORBIDDEN).entity("Expired Token").build();
			} else {
				Transaction tra = datastore.newTransaction();
				try {
					token = Entity.newBuilder(token).set("token_end", System.currentTimeMillis()).build();
					tra.update(token);
					tra.commit();
					return Response.ok("Logged out successfully").build();
				} finally {
					if (tra.isActive())
						tra.rollback();
				}
			}

		} else {
			return Response.status(Status.FORBIDDEN).entity("Invalid token").build();
		}
	}
}
