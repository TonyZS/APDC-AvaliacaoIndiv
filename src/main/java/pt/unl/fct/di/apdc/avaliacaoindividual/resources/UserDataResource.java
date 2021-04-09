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
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.avaliacaoindividual.util.GetUserData;
import pt.unl.fct.di.apdc.avaliacaoindividual.util.UserData;

@Path("/userdata")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UserDataResource {

	private static final Logger LOG = Logger.getLogger(UserDataResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private Gson g = new Gson();

	public UserDataResource() {
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserData(GetUserData data) {
		LOG.fine("Get User data attempt");
		Transaction tra = datastore.newTransaction();
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.tokenID);
		Entity token = datastore.get(tokenKey);
		try {
			if (token != null) {
				long tokenEnd = token.getLong("token_end");
				if (tokenEnd < System.currentTimeMillis()) {
					tra.rollback();
					return Response.status(Status.FORBIDDEN).entity("Expired Token.").build();
				} else {
					Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.getString("token_username"));
					Entity user = datastore.get(userKey);
					if (user.getString("user_role").equals("GA")) {
						Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.targetUsername);
						Entity target = datastore.get(targetKey);
						if (target == null) {
							tra.rollback();
							return Response.status(Status.FORBIDDEN).entity("User does not exist.").build();
						} else {
							UserData userdata = new UserData(target);
							tra.commit();
							return Response.ok(g.toJson(userdata)).build();
						}
					} else {
						tra.rollback();
						return Response.status(Status.FORBIDDEN)
								.entity("You dont have premission to access other users data").build();
					}
				}
			} else {
				tra.rollback();
				return Response.status(Status.FORBIDDEN).entity("Invalid Token.").build();
			}
		} finally {
			if (tra.isActive())
				tra.rollback();
		}
	}
}
