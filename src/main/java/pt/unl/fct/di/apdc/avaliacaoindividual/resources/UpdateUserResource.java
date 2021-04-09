package pt.unl.fct.di.apdc.avaliacaoindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.avaliacaoindividual.util.UpdateUserData;

@Path("/userupdate")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UpdateUserResource {

	private static final Logger LOG = Logger.getLogger(UpdateUserResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public UpdateUserResource() {
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegister(UpdateUserData data) {
		LOG.fine("Change attempt");
		Transaction tra = datastore.newTransaction();
		try {
			Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.tokenID);
			Entity token = datastore.get(tokenKey);

			if (token == null) {
				tra.rollback();
				return Response.status(Status.FORBIDDEN).entity("Invalid Token").build();
			}
			long tokenEnd = token.getLong("token_end");
			if (tokenEnd < System.currentTimeMillis()) {
				tra.rollback();
				return Response.status(Status.FORBIDDEN).entity("Expired Token").build();
			}

			Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.getString("token_username"));
			Entity user = datastore.get(userKey);

			if (data.password != null && !data.password.equals("")) {
				if (!data.validPassword()) {
					tra.rollback();
					return Response.status(Status.BAD_REQUEST)
							.entity("Password must be at least 10 characters long, contain a number and no spaces.")
							.build();
				}
				if (!data.secondPassword()) {
					tra.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Passwords do not match.").build();
				}
				if (!DigestUtils.sha512Hex(data.currentPassword).equals(user.getString("user_password"))) {
					tra.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Wrong password.").build();
				}
				user = Entity.newBuilder(user).set("user_password", data.password).build();
			}

			if (data.email != null && !data.email.equals("")) {
				if (!data.validEmail()) {
					tra.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Invalid Email.").build();
				}
				user = Entity.newBuilder(user).set("user_email", data.email).build();
			}

			if (data.perfil != null && !data.perfil.equals("")) {
				if (!data.validPerfil()) {
					tra.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Profile must be Public or Private.").build();
				}
				user = Entity.newBuilder(user).set("user_profile", data.perfil).build();
			}

			if (data.telfFixo != null && !data.telfFixo.equals("")) {
				if (!data.validFixo()) {
					tra.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Invalid phone number.").build();
				}
				user = Entity.newBuilder(user).set("user_fixo", data.telfFixo).build();
			}

			if (data.telfMovel != null && !data.telfMovel.equals("")) {
				if (!data.validMovel()) {
					tra.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Invalid phone number.").build();
				}
				user = Entity.newBuilder(user).set("user_movel", data.telfMovel).build();
			}

			if (data.morada != null && !data.morada.equals("")) {
				user = Entity.newBuilder(user).set("user_morada", data.morada).build();
			}

			if (data.moradaComp != null && !data.moradaComp.equals("")) {
				user = Entity.newBuilder(user).set("user_moradaComp", data.moradaComp).build();
			}

			if (data.localidade != null && !data.localidade.equals("")) {
				user = Entity.newBuilder(user).set("user_localidade", data.localidade).build();
			}

			if (data.cp != null && !data.cp.equals("")) {
				if (!data.validCp()) {
					tra.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Invalid Zip Code").build();
				}
				user = Entity.newBuilder(user).set("user_cp", data.cp).build();
			}
			tra.update(user);
			LOG.info("User updated.");
			tra.commit();
			return Response.ok("User updated.").build();
		} finally {
			if (tra.isActive())
				tra.rollback();
		}
	}

}
