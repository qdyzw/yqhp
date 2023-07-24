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
package com.yqhp.agent.jshell;

import com.android.ddmlib.IDevice;
import com.yqhp.agent.androidtools.AndroidUtils;
import com.yqhp.agent.driver.AndroidDeviceDriver;
import com.yqhp.agent.driver.DeviceDriver;
import io.appium.java_client.AppiumDriver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * 为了减轻调用方负担，这里对外提供的api，都不抛受检异常，使用@SneakyThrows自动抛出非受检异常
 *
 * @author jiangyitao
 */
@Slf4j
public class DeviceYQHP extends YQHP {

    public DeviceYQHP(DeviceDriver deviceDriver) {
        super(deviceDriver);
    }

    @Override
    public String getName() {
        return "device";
    }

    private DeviceDriver deviceDriver() {
        return (DeviceDriver) driver;
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriver appiumDriver() {
        return deviceDriver().getOrCreateAppiumDriver();
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriver refreshAppiumDriver() {
        return deviceDriver().refreshAppiumDriver();
    }

    /**
     * @since 0.0.1
     */
    public DeviceYQHP cap(String key, Object value) {
        deviceDriver().setCapability(key, value);
        return this;
    }

    /**
     * @param uri url or filePath
     * @since 0.0.1
     */
    @SneakyThrows
    public void installApp(String uri) {
        deviceDriver().installApp(uri);
    }

    /**
     * @since 0.0.1
     */
    public void installApp(File file) {
        deviceDriver().installApp(file);
    }

    /**
     * 在android设备内执行shell命令
     * 注意: 这是在设备内部执行的命令，所以不需要加"adb shell"
     *
     * @since 0.0.1
     */
    public String androidShell(String shellCommand) {
        return AndroidUtils.executeShellCommand(getIDevice(), shellCommand);
    }

    /**
     * @since 0.0.5
     */
    public IDevice getIDevice() {
        return ((AndroidDeviceDriver) driver).getIDevice();
    }
}