/*
 *  Copyright (c) 2016-2017, Salesforce.com, Inc.
 *  All rights reserved.
 *  Licensed under the BSD 3-Clause license.
 *  For full license text, see the LICENSE.txt file in repo root
 *    or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.pyplyn.duct.etl.transform.infostatus;

import com.google.inject.AbstractModule;
import com.salesforce.pyplyn.util.MultibinderFactory;

/**
 * Defines the {@link InfoStatus} transform binding
 *
 * @author Mihai Bojin &lt;mbojin@salesforce.com&gt;
 * @since 3.0
 */
public class InfoStatusModule extends AbstractModule {
    @Override
    protected void configure() {
        MultibinderFactory.transformFunctions(binder()).addBinding().toInstance(InfoStatus.class);
    }
}
