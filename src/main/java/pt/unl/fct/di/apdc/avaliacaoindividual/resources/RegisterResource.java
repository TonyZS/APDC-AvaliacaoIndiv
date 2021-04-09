package pt.unl.fct.di.apdc.avaliacaoindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.avaliacaoindividual.util.NewUserData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

	/**
	 * A Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public RegisterResource() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegister(NewUserData data) {
		LOG.fine("Register attempt by " + data.username);

		if (!data.validRegistration()) {
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		}

		Transaction tra = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = datastore.get(userKey);
			if (user != null) {
				tra.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Username already in use.").build();
			}

			if (!data.validUsername()) {
				tra.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Username can't have spaces").build();
			}

			if (!data.validPassword()) {
				tra.rollback();
				return Response.status(Status.BAD_REQUEST)
						.entity("Password must be at least 10 characters long, contain a number and no spaces").build();
			}

			if (!data.validEmail()) {
				tra.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Invalid Email.").build();
			}

			if (!data.secondPassword()) {
				tra.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Passwords do not match.").build();
			}

			user = Entity.newBuilder(userKey).set("user_name", data.username)
					.set("user_password", DigestUtils.sha512Hex(data.password)).set("user_email", data.email)
					.set("user_role", "USER").set("user_state", "ENABLED").set("user_profile", "Private")
					.set("user_creationTime", Timestamp.now()).build();

			tra.add(user);
			LOG.info("User created " + data.username);
			tra.commit();
			return Response.ok("New user registered").build();
		} finally {
			if (tra.isActive())
				tra.rollback();
		}

	}

}
