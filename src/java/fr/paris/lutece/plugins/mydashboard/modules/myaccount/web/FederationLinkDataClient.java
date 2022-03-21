/*
 * Copyright (c) 2002-2017, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.mydashboard.modules.myaccount.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.oauth2.business.Token;
import fr.paris.lutece.plugins.oauth2.dataclient.AbstractDataClient;
import fr.paris.lutece.plugins.openamidentityclient.business.FederationLink;
import fr.paris.lutece.plugins.openamidentityclient.service.OpenamIdentityException;
import fr.paris.lutece.plugins.openamidentityclient.service.OpenamIdentityService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * UserInfoDataClient
 */
public class FederationLinkDataClient extends AbstractDataClient
{

    public static final String PROPERTY_FEDERATION_LINK_IDENTITY_PROVIDER="mydashboard-myaccount.federationLinkIdentityProvider";
    public static final String PROPERTY_FEDERATION_LINK_IDENTITY_FIELD_USER_NAME="mydashboard-myaccount.federationLinkIdentityFieldUserName";
    public static final String PROPERTY_FEDERATION_LINK_IDENTITY_FIELD_USER_ID="mydashboard-myaccount.federationLinkIdentityFieldUserId";
    
    

    private static ObjectMapper _mapper;

    static
    {
        _mapper = new ObjectMapper( );
        _mapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void handleToken( Token token, HttpServletRequest request, HttpServletResponse response )
    {
    
    	String  strFederationLinkStatus=MyDashboardFederationLinkComponent.FEDERATION_LINK_CREATTION_LINK_STATUS_KO  ;
    	LuteceUser user=null;
        try
        {
        	 user = SecurityService.getInstance(  ).getRegisteredUser( request );
             
             if ( user != null )
             {
            	 Map<String, Object> mapInfo = parse( getData( token ) );
            	 String strFederationLinkProvider=AppPropertiesService.getProperty(PROPERTY_FEDERATION_LINK_IDENTITY_PROVIDER);
            	 String strFederationLinkFieldUserId=AppPropertiesService.getProperty(PROPERTY_FEDERATION_LINK_IDENTITY_FIELD_USER_ID);
            	 String strFederationLinkFieldUserName=AppPropertiesService.getProperty(PROPERTY_FEDERATION_LINK_IDENTITY_FIELD_USER_NAME);
            	 
            	 FederationLink federationLink=new FederationLink(user.getName(), strFederationLinkProvider, (String)mapInfo.get(strFederationLinkFieldUserId),(String) mapInfo.get(strFederationLinkFieldUserName));		
            	 OpenamIdentityService.getService().createFederationLink(federationLink);
            	 strFederationLinkStatus=MyDashboardFederationLinkComponent.FEDERATION_LINK_CREATTION_LINK_STATUS_OK  ;
            	 
             }		
         
        }
        catch( IOException ex )
        {
            _logger.error( "Error parsing UserInfo ", ex );
 
        } catch (OpenamIdentityException e) {
        	   _logger.error( "Error creating federation link  for user", e );
        }
        finally {
        	  try {
        		  
        		   String strNextUrl = AppPropertiesService.getProperty(MyDashboardFederationLinkComponent.PROPERTY_FEDERAION_LINK_REDIRECT_URL );
                   strNextUrl += "&"+MyDashboardFederationLinkComponent.PARAMETER_FEDERATION_LINK_CREATTION_LINK_STATUS   + "=" + strFederationLinkStatus;

                   response.sendRedirect(AppPathService.getAbsoluteUrl( request, strNextUrl ));
			} catch (IOException e) {
				   _logger.error( "Error for redirect user after creation fedaration link ", e );			}
		}
      

    }

    /**
     * parse the JSON for a token
     * 
     * @param strJson
     *            The JSON
     * @return The UserInfo
     * @throws java.io.IOException
     *             if an error occurs
     */
    Map<String, Object> parse( String strJson ) throws IOException
    {
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>( )
        {
        };

        return _mapper.readValue( strJson, typeRef );
    }

    @Override
    public void handleError( HttpServletRequest request, HttpServletResponse response, String strError )
    {
    	String  strFederationLinkStatus=MyDashboardFederationLinkComponent.FEDERATION_LINK_CREATTION_LINK_STATUS_KO  ;
        try
        {

        	
     	   String strNextUrl = AppPropertiesService.getProperty( MyDashboardFederationLinkComponent.PROPERTY_FEDERAION_LINK_REDIRECT_URL );
           AppPathService.getAbsoluteUrl( request, strNextUrl );
     	   
     	   strNextUrl += "&"+MyDashboardFederationLinkComponent.PARAMETER_FEDERATION_LINK_CREATTION_LINK_STATUS   + "=" + strFederationLinkStatus;

		response.sendRedirect( strNextUrl );
        }
        catch( IOException e )
        {
            AppLogService.error( "Error during federation linked", e );
        }

    }
}
