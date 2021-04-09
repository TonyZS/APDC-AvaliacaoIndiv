package pt.unl.fct.di.apdc.avaliacaoindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import pt.unl.fct.di.apdc.avaliacaoindividual.util.DeleteData;

@Path("/delete")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DeleteResource {

	private static final Logger LOG = Logger.getLogger(DeleteResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public DeleteResource() {
	}

	@DELETE
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogout(DeleteData data) {
		LOG.fine("Logout attempt");
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.tokenID);
		Entity token = datastore.get(tokenKey);
		if (token != null) {
			long tokenEnd = token.getLong("token_end");
			if (tokenEnd < System.currentTimeMillis()) {
				return Response.status(Status.FORBIDDEN).entity("Expired Token.").build();
			} else {
				Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
				Entity target = datastore.get(targetKey);
				if (!target.getString("user_role").equals("USER")) {
					return Response.status(Status.FORBIDDEN).entity("This user cannot be deleted.").build();
				} else {
					Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.getString("token_username"));
					Entity user = datastore.get(userKey);
					if (!user.getString("user_role").equals("USER")
							|| !user.getString("user_name").equals(data.username)) {
						return Response.status(Status.FORBIDDEN)
								.entity("You dont have the premission to delete this user.").build();
					} else {
						Transaction tra = datastore.newTransaction();
						try {
							tra.delete(tokenKey);
							tra.delete(targetKey);
							tra.commit();
							return Response.ok("User " + data.username + " deleted with success.").build();
						} finally {
							if (tra.isActive())
								tra.rollback();
						}
					}
				}
			}

		} else {
			return Response.status(Status.FORBIDDEN).entity("Invalid token.").build();
		}
	}

}
