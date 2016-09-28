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

import fr.paris.lutece.plugins.crm.business.demand.Demand;
import fr.paris.lutece.plugins.crm.business.demand.DemandFilter;
import fr.paris.lutece.plugins.crm.business.user.CRMUser;
import fr.paris.lutece.plugins.crm.service.demand.DemandService;
import fr.paris.lutece.plugins.crm.service.demand.DemandStatusCRMService;
import fr.paris.lutece.plugins.crm.service.demand.DemandTypeService;
import fr.paris.lutece.plugins.crm.service.parameters.AdvancedParametersService;
import fr.paris.lutece.plugins.crm.util.constants.CRMConstants;
import fr.paris.lutece.plugins.mydashboard.modules.myaccount.business.CrmDemandWraper;
import fr.paris.lutece.plugins.mydashboard.modules.myaccount.business.IDemandWraper;
import fr.paris.lutece.plugins.mydashboard.modules.myaccount.business.MessageDemandWrapper;
import fr.paris.lutece.plugins.parisconnect.business.Message;
import fr.paris.lutece.plugins.parisconnect.business.UserInformations;
import fr.paris.lutece.plugins.parisconnect.service.ParisConnectService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MyDemandService implements IMyDemandService
{
    private static IMyDemandService _singleton;
    private static final String BEAN_DEMAND_SERVICE = "mydashboard-myaccount.myDemandService";

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
        DemandFilter dFilter = new DemandFilter(  );
        dFilter.setIdCRMUser( crmUser.getIdCRMUser(  ) );

        List<IDemandWraper> listDemandWraper = new ArrayList<IDemandWraper>(  );
        List<Demand> listDemand = DemandService.getService(  ).findByFilter( dFilter );

        //Put All CRM demand
        if ( listDemand != null )
        {
            for ( Demand demand : listDemand )
            {
                listDemandWraper.add( new CrmDemandWraper( demand ) );
            }
        }

        //Put All Message Demand
        UserInformations currentUserInformations = ParisConnectService.getInstance(  )
                                                                      .getUser( crmUser.getUserGuid(  ), true );

        if ( ( currentUserInformations != null ) && ( currentUserInformations.getIdUsers(  ) != null ) )
        {
            List<Message> listUserMessage = ParisConnectService.getInstance(  )
                                                               .getUserMessages( currentUserInformations.getIdUsers(  ) );

            if ( listUserMessage != null )
            {
                for ( Message message : listUserMessage )
                {
                    listDemandWraper.add( new MessageDemandWrapper( message ) );
                }
            }
        }

        return listDemandWraper;
    }
}
