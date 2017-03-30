/*
 *  Copyright (c) 2016-2017, Salesforce.com, Inc.
 *  All rights reserved.
 *  Licensed under the BSD 3-Clause license.
 *  For full license text, see the LICENSE.txt file in repo root
 *    or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.pyplyn.util;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.salesforce.pyplyn.model.Extract;
import com.salesforce.pyplyn.model.Load;
import com.salesforce.pyplyn.model.Transform;
import com.salesforce.pyplyn.processor.ExtractProcessor;
import com.salesforce.pyplyn.processor.LoadProcessor;

/**
 * Builds {@link com.google.inject.Guice} implementations of {@link AbstractModule} on the fly
 *
 * @author Mihai Bojin &lt;mbojin@salesforce.com&gt;
 * @since 6.0
 */
public class ModuleBuilder {

    /**
     * Defines bindings for a model and its specified {@link ExtractProcessor}
     * <p/>Creates a {@link com.google.inject.Guice} module overriding its {@link AbstractModule#configure()} method
     *
     * @param model     The model class to bind; must extend {@link Extract}
     * @param processor The corresponding processor; must extend {@link ExtractProcessor} of the same type as the specified model
     * @param <T>       The extract type for which the bindings are configured
     */
    public static <T extends Extract> AbstractModule forExtract(final Class<T> model, final Class<? extends ExtractProcessor<T>> processor) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                // register the model
                MultibinderFactory.extractDatasources(binder()).addBinding().toInstance(model);

                // registers the extract processor
                MultibinderFactory.extractProcessors(binder()).addBinding().to(processor).in(Scopes.SINGLETON);
            }
        };
    }

    /**
     * Creates an module overriding its {@link AbstractModule#configure()} method
     *   to define the binding for the specified class
     *
     * @param cls Class to bind; must extend {@link Transform}
     */
    public static AbstractModule forTransform(final Class<? extends Transform> cls) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                MultibinderFactory.transformFunctions(binder()).addBinding().toInstance(cls);
            }
        };
    }

    /**
     * Defines bindings for a model and its specified {@link LoadProcessor}
     * <p/>Creates a {@link com.google.inject.Guice} module overriding its {@link AbstractModule#configure()} method
     *
     * @param model     The model class to bind; must extend {@link Load}
     * @param processor The corresponding processor; must extend {@link LoadProcessor} of the same type as the specified model
     * @param <T>       The load type for which the bindings are configured
     */
    public static <T extends Load> AbstractModule forLoad(final Class<T> model, final Class<? extends LoadProcessor<T>> processor) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                // register the model
                MultibinderFactory.loadDestinations(binder()).addBinding().toInstance(model);

                // registers the extract processor
                MultibinderFactory.loadProcessors(binder()).addBinding().to(processor).in(Scopes.SINGLETON);
            }
        };
    }
}
