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
package fr.paris.lutece.plugins.mydashboard.modules.myaccount.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.crm.business.demand.Demand;
import fr.paris.lutece.plugins.crm.business.demand.DemandFilter;
import fr.paris.lutece.plugins.crm.business.demand.category.Category;
import fr.paris.lutece.plugins.crm.business.user.CRMUser;
import fr.paris.lutece.plugins.crm.service.category.CategoryService;
import fr.paris.lutece.plugins.crm.service.demand.DemandService;
import fr.paris.lutece.plugins.crm.service.demand.DemandStatusCRMService;
import fr.paris.lutece.plugins.crm.service.demand.DemandTypeService;
import fr.paris.lutece.plugins.crm.service.parameters.AdvancedParametersService;
import fr.paris.lutece.plugins.crm.util.constants.CRMConstants;
import fr.paris.lutece.plugins.mydashboard.modules.myaccount.business.CrmDemandWraper;
import fr.paris.lutece.plugins.mydashboard.modules.myaccount.business.IDemandWraper;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPathService;


public class MyDemandService implements IMyDemandService
{
    private static IMyDemandService _singleton;
    private static final String BEAN_DEMAND_SERVICE = "mydashboard-myaccount.myDemandService";
    private static final String MARK_USER_INFORMATION_HASH = "user_informations_hash";
    private static final String MARK_ID_CURRENT_USER = "id_current_user";
    private static final String MARK_DEMANDS_LIST = "demands_list";
    private static final String PLUGIN_CRM = "crm";
    private static final String PLUGIN_PARIS_CONNECT = "parisconnect";
    private static final String DISPLAY_DEMAND_PARIS_CONNECT="mydashboard-myaccount.displayDemandParisConnect";
    
    

    public static IMyDemandService getInstance(  )
    {
        if ( _singleton == null )
        {
            _singleton = SpringContextService.getBean( BEAN_DEMAND_SERVICE );
        }

        return _singleton;
    }

    @Override
    public List<IDemandWraper> getAllUserDemand( CRMUser crmUser )
    {
        
        List<IDemandWraper> listDemandWraper = new ArrayList<IDemandWraper>(  );
        
        
       
            DemandFilter dFilter = new DemandFilter(  );
            dFilter.setIdCRMUser( crmUser.getIdCRMUser(  ) );
    
            
            
    
            
            List<Demand> listDemand = DemandService.getService(  ).findByFilter( dFilter );
    
            //Put All CRM demand
            if ( listDemand != null )
            {
                for ( Demand demand : listDemand )
                {
                    listDemandWraper.add( new CrmDemandWraper( demand ) );
                }
            }
  

        return listDemandWraper;
    }
    
    
    @Override
    public List<IDemandWraper> getUserDemandByCategory( CRMUser crmUser, String categoryCode )
    {
        List<IDemandWraper> listDemandWraper = new ArrayList<IDemandWraper>(  );
        
        Category category = CategoryService.getService( ).findByCode( categoryCode );
        
        if ( category != null )
        {
            DemandFilter dFilter = new DemandFilter(  );
            dFilter.setIdCRMUser( crmUser.getIdCRMUser(  ) );
            
            List<Demand> listDemand = DemandService.getService(  ).findByFilter( dFilter );
            
            if ( listDemand != null )
            {
                for ( Demand demand : listDemand )
                {
                    if ( DemandTypeService.getService( ).findByPrimaryKey( demand.getIdDemandType( ) ).getIdCategory( ) == category.getIdCategory( ) )
                    {
                        listDemandWraper.add( new CrmDemandWraper( demand ) );
                    }
                }
            }
        }
        
        return listDemandWraper;
    }
 
    public void addInformations(HttpServletRequest request ,CRMUser crmUser,List<IDemandWraper>listDemand,Map<String, Object> model)
    {
	
	   //Crm Informations
        model.put( CRMConstants.MARK_STATUS_CRM_LIST,
            DemandStatusCRMService.getService(  ).getAllStatusCRM( request.getLocale(  ) ) );
        model.put( CRMConstants.MARK_DISPLAYDRAFT,
            AdvancedParametersService.getService(  ).isParameterValueByKey( CRMConstants.CONSTANT_DISPLAYDRAFT ) );
        model.put( CRMConstants.MARK_LOCALE, request.getLocale(  ) );
        model.put( CRMConstants.MARK_DEMAND_TYPES_LIST, DemandTypeService.getService(  ).findAll(  ) );
        model.put( CRMConstants.MARK_MAP_DO_LOGIN, SecurityService.getInstance(  ).getLoginPageUrl(  ) );
        model.put( CRMConstants.MARK_BASE_URL, AppPathService.getBaseUrl( request ) );

    
       
        
        model.put( CRMConstants.MARK_CRM_USER, crmUser );
        model.put( MARK_DEMANDS_LIST, listDemand );

    }
    
  
}
