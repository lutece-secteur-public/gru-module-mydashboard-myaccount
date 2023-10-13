/*
 * Copyright (c) 2002-2023, City of Paris
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.mydashboard.service.MyDashboardComponent;
import fr.paris.lutece.plugins.openamidentityclient.business.Account;
import fr.paris.lutece.plugins.openamidentityclient.service.OpenamIdentityException;
import fr.paris.lutece.plugins.openamidentityclient.service.OpenamIdentityService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.web.l10n.LocaleService;
import fr.paris.lutece.util.html.HtmlTemplate;

/*
 * Dashboard for Login Informations
 */
public class MyDashboardLoginInfosComponent extends MyDashboardComponent
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final String DASHBOARD_COMPONENT_ID = "mydashboard.myDashboardComponentLoginInfos";
    private static final String MESSAGE_DASHBOARD_COMPONENT_DESCRIPTION = "module.mydashboard.myaccount.component.loginInfos.description";
    private static final String TEMPLATE_DASHBOARD_COMPONENT = "/skin/plugins/mydashboard/modules/myaccount/login_infos_component.html";

    private static final String MARK_LOGIN_EMAIL = "login_email";
    private static final String MARK_LAST_MODIFY_PASSWORD_DATE = "last_modify_password_date";

    @Override
    public String getDashboardData( HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<>( );

        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );
        try
        {
            Account account = OpenamIdentityService.getService( ).getAccount( user.getName( ) );
            model.put( MARK_LOGIN_EMAIL, account.getLogin( ) );
            if ( account.getModificationDate( ) != null && account.getModificationDate( ).length( ) > 0 )
            {
                LocalDate date = LocalDateTime.parse( account.getModificationDate( ).substring( 0, account.getModificationDate( ).length( ) - 1 ),
                        DateTimeFormatter.ofPattern( "uuuuMMddHHmmss" ) ).toLocalDate( );
                String lastUpdateDate = date.format( DateTimeFormatter.ofPattern( "dd MMMM yyyy" ) );

                model.put( MARK_LAST_MODIFY_PASSWORD_DATE, lastUpdateDate );
            }
        }
        catch( OpenamIdentityException e )
        {
            AppLogService
                    .error( "Une erreur est survenue lors de la recuperation d'un utilisateur en utilisant son guid le code d'erreur renvoye par l'API est: "
                            + e.getErrorCode( ) + " le guid est: " + user.getName( ) );
        }

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_DASHBOARD_COMPONENT, LocaleService.getDefault( ), model );

        return template.getHtml( );
    }

    @Override
    public String getComponentDescription( Locale locale )
    {
        return I18nService.getLocalizedString( MESSAGE_DASHBOARD_COMPONENT_DESCRIPTION, locale );
    }

    @Override
    public String getComponentId( )
    {
        return DASHBOARD_COMPONENT_ID;
    }

}
