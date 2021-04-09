package pt.unl.fct.di.apdc.avaliacaoindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import pt.unl.fct.di.apdc.avaliacaoindividual.util.AuthToken;
import pt.unl.fct.di.apdc.avaliacaoindividual.util.LoginData;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	/**
	 * A Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private Gson g = new Gson();

	public LoginResource() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by " + data.username);
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);
		if (user != null) {
			String hashpassword = user.getString("user_password");
			if (hashpassword.equals(DigestUtils.sha512Hex(data.password))) {
				AuthToken token = new AuthToken(data.username);
				Transaction tra = datastore.newTransaction();
				Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(token.tokenID);
				try {
					Entity toke = Entity.newBuilder(tokenKey).set("token_username", token.username)
							.set("token_role", user.getString("user_role")).set("token_start", token.creationDate)
							.set("token_end", token.expirationDate).build();
					tra.add(toke);
					tra.commit();
				} finally {
					if (tra.isActive())
						tra.rollback();
				}
				return Response.ok(g.toJson(token)).build();
			} else {
				return Response.status(Status.FORBIDDEN).entity("Wrong password").build();
			}
		} else {
			return Response.status(Status.FORBIDDEN).entity("Invalid username").build();
		}

	}

}
