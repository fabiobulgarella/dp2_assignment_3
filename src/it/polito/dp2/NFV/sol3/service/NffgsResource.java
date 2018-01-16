package it.polito.dp2.NFV.sol3.service;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.NFV.sol3.jaxb.NffgType;

@Path("/nffgs")
@Api(value = "/nffgs", description = "a collection of nffg objects")
public class NffgsResource
{
	// Instantiate NffgsService in charge of execute all needed operations
	NfvDeployerService nfvService = new NfvDeployerService();
	
	// GET list of Nffg objects
	@GET
	@ApiOperation(value = "get list of Nffg objects ", notes = "text plain format")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 500, message = "Internal Server Error") })
	@Produces(MediaType.APPLICATION_XML)
	public Set<NffgType> getNffg()
	{
		try {
			Set<NffgType> nffg_list = nfvService.getNffgs();
			return nffg_list;
		}
		catch (Exception e) {
			throw new InternalServerErrorException();
		}
	}
}
