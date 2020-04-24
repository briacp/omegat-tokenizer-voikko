/**************************************************************************
${PLUGIN_TITLE}
Copyright (C) 2020 Briac Pilpre

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
**************************************************************************/
package net.briac.omegat.plugin;

import java.util.Locale;
import java.util.ResourceBundle;

import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.util.Preferences;

public class PluginSkeleton {
    public static final String PLUGIN_NAME = PluginSkeleton.class.getPackage().getImplementationTitle();
    public static final String PLUGIN_VERSION = PluginSkeleton.class.getPackage().getImplementationVersion();
    public static final ResourceBundle RES = ResourceBundle.getBundle("plugin-skeleton", Locale.getDefault());
    private static final Logger LOGGER = Logger.getLogger(LqePlugin.class.getName());

    public static void loadPlugins() {
        //Core.registerFilterClass(clazz);
        //Core.registerMachineTranslationClass(clazz);
        //Core.registerMarker(marker);
        //Core.registerMarkerClass(clazz);
        //Core.registerTokenizerClass(clazz);

        //CoreEvents.registerEditorEventListener(listener);
        //CoreEvents.registerEntryEventListener(listener);
        //CoreEvents.registerFontChangedEventListener(listener);
        //CoreEvents.registerProjectChangeListener(listener);

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            @Override
            public void onApplicationStartup() {
                /* empty */
            }

            @Override
            public void onApplicationShutdown() {
                /* empty */
            }
        });
    }

    public static void unloadPlugins() {
        /* empty */
    }
}
