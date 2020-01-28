/*
 * Copyright (c) 2002-2016, Mairie de Paris
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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.openamidentityclient.service.OpenamIdentityException;
import fr.paris.lutece.plugins.openamidentityclient.service.OpenamIdentityService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.xpage.MVCApplication;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.portal.web.xpages.XPage;


@Controller( xpageName = "federationLinkMyAcount", pageTitleI18nKey = "module.mydashboard.myaccount.fedederationLinkMyAccountApp.pageTitle", pagePathI18nKey = "module.mydashboard.myaccount.fedederationLinkMyAccountApp.pageLabel" )
public class FederationLinkMyAccountApp extends MVCApplication
{
    private static final String ACTION_DELETE_FEDERATION_LINK = "delete_federation_link";
   
    // Parameters
    private static final String PARAMETER_FEDERATION_LINK_PROVIDER = "federation_link_provider";
  
    //Marker
  

    @Action( value = ACTION_DELETE_FEDERATION_LINK)
    public XPage deleteFederationLink( HttpServletRequest request )
    {
        String strNextUrl=null;
        LuteceUser user = SecurityService.getInstance(  ).getRegisteredUser( request );
        String strFederationLinkProvider=request.getParameter( PARAMETER_FEDERATION_LINK_PROVIDER );
     
        try
        {
        if ( user != null && strFederationLinkProvider!=null )
        {
            
           
                OpenamIdentityService.getService().deleteFederationLink( user.getName( ), strFederationLinkProvider );
            
                
                
        }
        }
        catch( OpenamIdentityException e )
        {
          
            AppLogService.error( e );
            
        }
        finally {
             strNextUrl = AppPropertiesService.getProperty(MyDashboardFederationLinkComponent.PROPERTY_FEDERAION_LINK_REDIRECT_URL );
            
        }
        return redirect( request,AppPathService.getAbsoluteUrl( request, strNextUrl ) );
       

    }

   
    
}
