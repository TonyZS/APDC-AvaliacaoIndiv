package pt.unl.fct.di.apdc.avaliacaoindividual.resources;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.LinkedList;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.avaliacaoindividual.util.ListRoleData;

@Path("/listrole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListUsersRoleResource {

	private static final Logger LOG = Logger.getLogger(ListUsersRoleResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private Gson g = new Gson();

	public ListUsersRoleResource() {
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserData(ListRoleData data) throws EntityNotFoundException {
		LOG.fine("List Role attempt");
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
					if (user.getString("user_role").equals("GBO")) {
						List<String> roleList = listRole(data.targetRole);
						if (roleList.isEmpty()) {
							tra.rollback();
							return Response.ok("There are no users with that role").build();
						} else {
							tra.commit();
							return Response.ok(g.toJson(roleList)).build();
						}
					} else {
						tra.rollback();
						return Response.status(Status.FORBIDDEN).entity("You dont have premission to access role list")
								.build();
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

	private List<String> listRole(String role) throws EntityNotFoundException {
		DatastoreService data = DatastoreServiceFactory.getDatastoreService();
		Filter filter = new FilterPredicate("user_role", FilterOperator.EQUAL, role);
		Query q = new Query("User").setFilter(filter);

		PreparedQuery pq = data.prepare(q);
		List<com.google.appengine.api.datastore.Entity> u = pq.asList(FetchOptions.Builder.withDefaults());
		List<String> l = new LinkedList<String>();
		for (int i = 0; i < u.size(); i++) {
			com.google.appengine.api.datastore.Entity user = data.get(u.get(i).getKey());
			String s = (String) user.getProperty("user_name");
			l.add(s);
		}
		return l;

	}
}
