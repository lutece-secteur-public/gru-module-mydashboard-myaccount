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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.crm.business.demand.Demand;
import fr.paris.lutece.plugins.crm.business.demand.DemandType;
import fr.paris.lutece.plugins.crm.business.notification.Notification;
import fr.paris.lutece.plugins.crm.business.notification.NotificationFilter;
import fr.paris.lutece.plugins.crm.business.user.CRMUser;
import fr.paris.lutece.plugins.crm.service.demand.DemandService;
import fr.paris.lutece.plugins.crm.service.demand.DemandTypeService;
import fr.paris.lutece.plugins.crm.service.notification.NotificationService;
import fr.paris.lutece.plugins.crm.service.user.CRMUserService;
import fr.paris.lutece.plugins.crm.util.constants.CRMConstants;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.xpage.MVCApplication;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.portal.web.xpages.XPage;


@Controller( xpageName = "demandMyAcount", pageTitleI18nKey = "module.mydashboard.myaccount.demandMyAccountApp.pageTitle", pagePathI18nKey = "module.mydashboard.myaccount.demandMyAccountApp.pageLabel" )
public class DemandMyAccountApp extends MVCApplication
{
    private static final String VIEW_NOTIFICATIONS = "view_notifications";
    private static final String VIEW_MESSAGES = "view_messages";
    private static final String PARIS_CONNECT_CONSTANT_UNREAD="0";

    // Parameters
    private static final String PARAMETER_ID_MESSAGE = "id_message";
    private static final String TEMPLATE_NOTIFICATION_CRM = "/skin/plugins/mydashboard/modules/myaccount/notification_crm_list.html";
    private static final String TEMPLATE_MESSAGE_LIST = "/skin/plugins/mydashboard/modules/myaccount/message_list.html";

    //Marker
    private static final String MARK_USER_MESSAGE_LIST = "user_message_list";
    private static final String MARK_USER_INFORMATION_HASH = "user_informations_hash";
    private static final String MARK_LUTECE_USER_INFORMATIONS = "user_lutece_informations";
    private static final String MARK_ID_CURRENT_USER = "id_current_user";
    private static final String MARK_ID_DEMAND = "id_demand";
    private static final String MARK_ID_MESSAGE= "id_message";
    private static final String MARK_AVATAR_URL = "avatar_url";
    

    @View( value = VIEW_NOTIFICATIONS, defaultView = true )
    public XPage getViewNotificationCrm( HttpServletRequest request )
    {
        LuteceUser user = SecurityService.getInstance(  ).getRegisteredUser( request );
        Map<String, Object> model = getModel(  );

        if ( user != null )
        {
            CRMUser crmUser = CRMUserService.getService(  ).findByUserGuid( user.getName(  ) );
            String strIdDemand = request.getParameter( CRMConstants.PARAMETER_ID_DEMAND );
            
            if ( ( crmUser != null ) && StringUtils.isNotBlank( strIdDemand ) && StringUtils.isNumeric( strIdDemand ) )
            {
                int nIdDemand = Integer.parseInt( strIdDemand );
                model.put(MARK_ID_DEMAND, nIdDemand);
                Demand demand = DemandService.getService(  ).findByPrimaryKey( nIdDemand );

                if ( ( demand != null ) && ( crmUser.getIdCRMUser(  ) == demand.getIdCRMUser(  ) ) )
                {
                    NotificationFilter nFilter = new NotificationFilter(  );
                    nFilter.setIdDemand( nIdDemand );
                    List<Notification> listNotification= NotificationService.getService(  ).findByFilter( nFilter );
                    DemandType demandType = DemandTypeService.getService(  ).findByPrimaryKey( demand.getIdDemandType(  ) );
                    model.put( CRMConstants.MARK_NOTIFICATIONS_LIST,
                            listNotification );
                    model.put( CRMConstants.MARK_DEMAND_TYPE, demandType );
                    //update unread Flag
                    if(!CollectionUtils.isEmpty( listNotification ))
                    {
                      for(Notification notification:listNotification)
                      {
                          if(!notification.isRead( ))
                          {
                              Notification notificationSave= NotificationService.getService( ).findByPrimaryKey( notification.getIdNotification( ) );
                              notificationSave.setIsRead( true );
                              NotificationService.getService( ).update( notificationSave );
                          }
                        }
                        
                    }
                    
                }
            }
        }

        XPage xpage = getXPage( TEMPLATE_NOTIFICATION_CRM, getLocale( request ), model );
        xpage.setStandalone( true );

        return xpage;
    }


    
}
