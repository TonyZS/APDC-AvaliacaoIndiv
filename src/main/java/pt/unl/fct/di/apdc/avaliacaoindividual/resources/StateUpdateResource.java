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

import pt.unl.fct.di.apdc.avaliacaoindividual.util.StateUpdateData;

@Path("/stateupdate")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class StateUpdateResource {

	private static final Logger LOG = Logger.getLogger(StateUpdateResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public StateUpdateResource() {
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doStateUpdate(StateUpdateData data) {
		LOG.fine("Role update attempt");
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.tokenID);
		Entity token = datastore.get(tokenKey);
		Transaction tra = datastore.newTransaction();
		if (token != null) {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.getString("token_username"));
			Entity user = datastore.get(userKey);
			Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity target = datastore.get(targetKey);
			long tokenEnd = token.getLong("token_end");
			if (tokenEnd < System.currentTimeMillis()) {
				tra.rollback();
				return Response.status(Status.FORBIDDEN).entity("Expired Token.").build();
			} else {
				if (target == null) {
					tra.rollback();
					return Response.status(Status.FORBIDDEN).entity("Invalid username.").build();
				} else {
					if (!data.possibleUpdate(user.getString("user_role"), target.getString("user_role"))) {
						tra.rollback();
						return Response.status(Status.FORBIDDEN).entity("Invalid state change").build();
					} else {
						try {
							target = Entity.newBuilder(target).set("user_state", data.newState).build();
							tra.update(target);
							tra.commit();
							return Response.ok("State updated").build();

						} finally {
							if (tra.isActive())
								tra.rollback();
						}
					}
				}
			}
		} else {
			tra.rollback();
			return Response.status(Status.FORBIDDEN).entity("Invalid Token.").build();
		}
	}
}
