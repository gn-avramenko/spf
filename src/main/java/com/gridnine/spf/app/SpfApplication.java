/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: SPF
 *****************************************************************/
package com.gridnine.spf.app;

import java.util.Properties;

public interface SpfApplication {

     void start(Properties config) throws Exception;

     void stop() throws Exception;
}
