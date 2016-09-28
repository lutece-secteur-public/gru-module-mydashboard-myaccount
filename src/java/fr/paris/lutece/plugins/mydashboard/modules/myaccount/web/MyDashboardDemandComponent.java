/*
 * Copyright (c) 2002-2015, Mairie de Paris
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

import fr.paris.lutece.plugins.crm.business.user.CRMUser;
import fr.paris.lutece.plugins.crm.service.demand.DemandStatusCRMService;
import fr.paris.lutece.plugins.crm.service.demand.DemandTypeService;
import fr.paris.lutece.plugins.crm.service.parameters.AdvancedParametersService;
import fr.paris.lutece.plugins.crm.service.user.CRMUserAttributesService;
import fr.paris.lutece.plugins.crm.service.user.CRMUserService;
import fr.paris.lutece.plugins.crm.util.constants.CRMConstants;
import fr.paris.lutece.plugins.mydashboard.modules.myaccount.business.IDemandWraper;
import fr.paris.lutece.plugins.mydashboard.modules.myaccount.service.MyDemandService;
import fr.paris.lutece.plugins.mydashboard.service.MyDashboardComponent;
import fr.paris.lutece.plugins.parisconnect.business.Message;
import fr.paris.lutece.plugins.parisconnect.business.UserInformations;
import fr.paris.lutece.plugins.parisconnect.service.ParisConnectService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.l10n.LocaleService;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * MyDashboardCRMComponent
 */
public class MyDashboardDemandComponent extends MyDashboardComponent
{
    private static final String DASHBOARD_COMPONENT_ID = "mydashboard-mycaccount.demandsComponent";
    private static final String MESSAGE_DASHBOARD_COMPONENT_DESCRIPTION = "module.mydashboard.myaccount.component.demands.description";
    private static final String TEMPLATE_DASHBOARD_COMPONENT = "/skin/plugins/mydashboard/modules/myaccount/demands_component.html";
    private static final String MARK_XPAGE_MYDASHBOARD = "mydashboard";
    private static final String MARK_DEMANDS_LIST = "demands_list";
    private static final String MARK_USER_INFORMATION_HASH = "user_informations_hash";
    private static final String MARK_ID_CURRENT_USER = "id_current_user";

    @Override
    public String getDashboardData( HttpServletRequest request )
    {
        LuteceUser user = SecurityService.getInstance(  ).getRegisteredUser( request );

        createOrUpdateCRMAccount( user );

        CRMUserService crmUserService = CRMUserService.getService(  );

        CRMUser crmUser = crmUserService.findByUserGuid( user.getName(  ) );

        if ( crmUser != null )
        {
            Map<String, Object> model = new HashMap<String, Object>(  );

            List<IDemandWraper> listDemandWraper = MyDemandService.getInstance(  ).getAllUserDemand( crmUser );

            //Crm Informations
            model.put( CRMConstants.MARK_STATUS_CRM_LIST,
                DemandStatusCRMService.getService(  ).getAllStatusCRM( request.getLocale(  ) ) );
            model.put( CRMConstants.MARK_DISPLAYDRAFT,
                AdvancedParametersService.getService(  ).isParameterValueByKey( CRMConstants.CONSTANT_DISPLAYDRAFT ) );
            model.put( CRMConstants.MARK_LOCALE, request.getLocale(  ) );
            model.put( CRMConstants.MARK_DEMAND_TYPES_LIST, DemandTypeService.getService(  ).findAll(  ) );
            model.put( CRMConstants.MARK_CRM_USER, crmUser );
            model.put( CRMConstants.MARK_MAP_DO_LOGIN, SecurityService.getInstance(  ).getLoginPageUrl(  ) );
            model.put( CRMConstants.MARK_BASE_URL, AppPathService.getBaseUrl( request ) );

            //Message Informations
            Map<String, UserInformations> mapUserInformations = null;
            UserInformations currentUserInformations = ParisConnectService.getInstance(  )
                                                                          .getUser( user.getName(  ), true );
            mapUserInformations = getUsersMapInformations( listDemandWraper );
            if(currentUserInformations!=null)
            {
        	model.put( MARK_ID_CURRENT_USER, currentUserInformations.getIdUsers(  ) );
            }
            model.put( MARK_USER_INFORMATION_HASH, mapUserInformations );
            //Sort demand 
            Collections.sort( listDemandWraper );

            model.put( MARK_DEMANDS_LIST, listDemandWraper );

            HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_DASHBOARD_COMPONENT,
                    LocaleService.getDefault(  ), model );

            return template.getHtml(  );
        }

        return "";
    }

    @Override
    public String getComponentId(  )
    {
        return DASHBOARD_COMPONENT_ID;
    }

    @Override
    public String getComponentDescription( Locale locale )
    {
        return I18nService.getLocalizedString( MESSAGE_DASHBOARD_COMPONENT_DESCRIPTION, locale );
    }

    /**
     * Create a CRM account if the current user does not have one
     *
     * @param user
     *            the LuteceUser
     */
    private void createOrUpdateCRMAccount( LuteceUser user )
    {
        if ( user != null )
        {
            CRMUser crmUser = CRMUserService.getService(  ).findByUserGuid( user.getName(  ) );

            if ( crmUser == null )
            {
                crmUser = new CRMUser(  );
                crmUser.setUserGuid( user.getName(  ) );
                crmUser.setStatus( CRMUser.STATUS_ACTIVATED );

                Map<String, String> userAttributes = new HashMap<String, String>(  );

                for ( String strUserAttributeKey : CRMUserAttributesService.getService(  ).getUserAttributeKeys(  ) )
                {
                    userAttributes.put( strUserAttributeKey, user.getUserInfo( strUserAttributeKey ) );
                }

                crmUser.setUserAttributes( userAttributes );
                CRMUserService.getService(  ).create( crmUser );
            }
            else if ( crmUser.isMustBeUpdated(  ) )
            {
                crmUser.setMustBeUpdated( false );
                crmUser.setStatus( CRMUser.STATUS_ACTIVATED );

                Map<String, String> userAttributes = new HashMap<String, String>(  );

                for ( String strUserAttributeKey : CRMUserAttributesService.getService(  ).getUserAttributeKeys(  ) )
                {
                    userAttributes.put( strUserAttributeKey, user.getUserInfo( strUserAttributeKey ) );
                }

                crmUser.setUserAttributes( userAttributes );
                CRMUserService.getService(  ).update( crmUser );
            }
        }
    }

    public static Map<String, UserInformations> getUsersMapInformations( List<IDemandWraper> lisdemandWraper )
    {
        Map<String, UserInformations> mapUserInformations = new HashMap<String, UserInformations>(  );
        UserInformations userInformations = null;
        Message message;

        for ( IDemandWraper demandWrapper : lisdemandWraper )
        {
            if ( demandWrapper.getType(  ).equals( IDemandWraper.DEMAND_MESSAGE_TYPE ) )
            {
                message = (Message) demandWrapper.getDemand(  );

                if ( !mapUserInformations.containsKey( message.getIdUsersFrom(  ) ) )
                {
                    userInformations = ParisConnectService.getInstance(  ).getUserName( message.getIdUsersFrom(  ), true );

                    if ( userInformations != null )
                    {
                        mapUserInformations.put( message.getIdUsersFrom(  ), userInformations );
                    }
                }

                if ( !mapUserInformations.containsKey( message.getIdUsersTo(  ) ) )
                {
                    userInformations = ParisConnectService.getInstance(  ).getUserName( message.getIdUsersTo(  ), true );

                    if ( userInformations != null )
                    {
                        mapUserInformations.put( message.getIdUsersTo(  ), userInformations );
                    }
                }
            }
        }

        return mapUserInformations;
    }
}
