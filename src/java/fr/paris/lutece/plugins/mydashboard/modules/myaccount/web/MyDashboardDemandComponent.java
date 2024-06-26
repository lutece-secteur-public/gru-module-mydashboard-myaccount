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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import fr.paris.lutece.plugins.crm.business.user.CRMUser;
import fr.paris.lutece.plugins.crm.service.user.CRMUserAttributesService;
import fr.paris.lutece.plugins.crm.service.user.CRMUserService;
import fr.paris.lutece.plugins.mydashboard.modules.myaccount.business.IDemandWraper;
import fr.paris.lutece.plugins.mydashboard.modules.myaccount.service.MyDemandService;
import fr.paris.lutece.plugins.mydashboard.service.MyDashboardComponent;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.l10n.LocaleService;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;


/**
 * MyDashboardCRMComponent
 */
public class MyDashboardDemandComponent extends MyDashboardComponent
{
    
    public static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    public static final String PROPERTY_URL_MES_DEMARCHES="mydashboard-myaccount.url.mesdemarches";
    
    private static final String DASHBOARD_COMPONENT_ID = "mydashboard-myaccount.demandsComponent";
    private static final String MESSAGE_DASHBOARD_COMPONENT_DESCRIPTION = "module.mydashboard.myaccount.component.demands.description";
    private static final String TEMPLATE_DASHBOARD_COMPONENT = "/skin/plugins/mydashboard/modules/myaccount/demands_component.html";
    private static final String MARK_XPAGE_MYDASHBOARD = "mydashboard";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String CURRENT_PAGE_INDEX = "current_page_index";
    private static final String PROPERTY_NUMBER_OF_DEMAND_PER_PAGE="mydashboard-myaccount.numberOfDemandPerPage";
    
    private static final String PARAMETER_ALL = "all";
    private static final String PARAMETER_CATEGORY_CODE = "cat";
    
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
            
            List<IDemandWraper> listDemandWraper = new ArrayList<IDemandWraper>(  );
            
            String categoryCode = request.getParameter( PARAMETER_CATEGORY_CODE );
            
            if ( PARAMETER_ALL.equals( categoryCode ) )
            {
                listDemandWraper = MyDemandService.getInstance(  ).getAllUserDemand( crmUser );
            }
            else if ( categoryCode != null && !categoryCode.trim( ).isEmpty( ) )
            {
                listDemandWraper = MyDemandService.getInstance(  ).getUserDemandByCategory( crmUser, categoryCode );
            }
            
            //Sort demand 
            Collections.sort( listDemandWraper );
            HttpSession session = request.getSession( true );

            String strCurrentPageIndex=session.getAttribute( CURRENT_PAGE_INDEX )!=null ?(String)session.getAttribute( CURRENT_PAGE_INDEX ):null  ;  
            strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, strCurrentPageIndex );
            session.setAttribute( CURRENT_PAGE_INDEX, strCurrentPageIndex );
            
    	    int nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_NUMBER_OF_DEMAND_PER_PAGE, 10 );
    	     // PAGINATOR
            LocalizedPaginator<IDemandWraper> paginator = new LocalizedPaginator<IDemandWraper>( listDemandWraper, nDefaultItemsPerPage, AppPropertiesService.getProperty(PROPERTY_URL_MES_DEMARCHES) + categoryCode, Paginator.PARAMETER_PAGE_INDEX, strCurrentPageIndex, request.getLocale(  ) );

            model.put( MARK_NB_ITEMS_PER_PAGE, "" + nDefaultItemsPerPage );
            model.put( MARK_PAGINATOR, paginator );
            
            MyDemandService.getInstance().addInformations(request, crmUser, paginator.getPageItems(  ), model);
         
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

    
}
