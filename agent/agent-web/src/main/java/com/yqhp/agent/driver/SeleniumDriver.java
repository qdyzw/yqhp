/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.agent.driver;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;

/**
 * @author jiangyitao
 */
@Slf4j
public abstract class SeleniumDriver extends Driver {

    private DesiredCapabilities capabilities = new DesiredCapabilities();
    protected DriverService driverService;
    private RemoteWebDriver webDriver;

    public void setCapability(String key, Object value) {
        capabilities.setCapability(key, value);
    }

    private void resetCapability() {
        capabilities = new DesiredCapabilities();
    }

    public synchronized DriverService getOrStartDriverService() {
        if (driverServiceIsRunning()) {
            return driverService;
        }
        driverService = startDriverService();
        return driverService;
    }

    protected abstract DriverService startDriverService();

    public synchronized void stopDriverService() {
        if (driverServiceIsRunning()) {
            log.info("Stop driverService...1");
            driverService.stop();
            if (driverServiceIsRunning()) {
                log.info("Stop driverService...2");
                driverService.stop();
            }
            driverService = null;
        }
    }

    private boolean driverServiceIsRunning() {
        return driverService != null && driverService.isRunning();
    }

    public synchronized RemoteWebDriver refreshWebDriver() {
        quitWebDriver();
        return getOrCreateWebDriver();
    }

    public synchronized RemoteWebDriver getOrCreateWebDriver() {
        if (webDriver != null) {
            return webDriver;
        }
        log.info("Create webDriver, capabilities: {}", capabilities);
        webDriver = newRemoteWebDriver(getOrStartDriverService(), capabilities);
        log.info("WebDriver created, capabilities: {}", capabilities);
        return webDriver;
    }

    public synchronized void quitWebDriver() {
        if (webDriver != null) {
            try {
                log.info("Quit webDriver");
                webDriver.quit();
            } catch (Exception e) {
                log.warn("Quit webDriver failed", e);
            }
            webDriver = null;
        }
    }

    protected abstract RemoteWebDriver newRemoteWebDriver(DriverService service, DesiredCapabilities capabilities);

    public <T> T screenshotAs(OutputType<T> outputType) {
        return getOrCreateWebDriver().getScreenshotAs(outputType);
    }

    @Override
    public void release() {
        quitWebDriver();
        stopDriverService();
        resetCapability();
        super.release();
    }
}