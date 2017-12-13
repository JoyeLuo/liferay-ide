/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.ui.layouttpl.tests;

import com.liferay.ide.ui.liferay.SwtbotBase;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Terry Jia
 */
public class NewLayouttplProjectSdkTests extends SwtbotBase {

	@AfterClass
	public static void cleanPluginsSdk() {
		jobAction.waitForIvy();

		String sdkName = envAction.getLiferayPluginsSdkName() + "-" + envAction.getTimestamp();

		viewAction.project.closeAndDeleteWithNoRunningJobs(sdkName);
	}

	@BeforeClass
	public static void createPluginsSdk() throws IOException {
		envAction.unzipPluginsSDK();

		viewAction.switchLiferayPerspective();

		wizardAction.openNewLiferayPluginProjectWizard();

		String projectName = "test-portlet";

		wizardAction.newPlugin.prepareSdk(projectName);

		wizardAction.next();

		wizardAction.next();

		String location = envAction.getLiferayPluginsSdkDir().toOSString();

		wizardAction.setSdkLocation.prepare(location);

		wizardAction.finish();

		jobAction.waitForIvy();

		jobAction.waitForValidate(projectName);

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createLayoutTemplate() {
		wizardAction.openNewLiferayPluginProjectWizard();

		String projectName = "test-template-layouttpl";

		wizardAction.newPlugin.prepareLayoutTemplateSdk(projectName);

		wizardAction.finish();

		jobAction.waitForIvy();

		jobAction.waitForValidate(projectName);

		wizardAction.openNewLiferayLayoutTemplate();

		wizardAction.finish();

		String layoutTpl = "test_template.tpl";

		viewAction.project.openFile(projectName, "docroot", layoutTpl);

		editorAction.close();

		String layoutWapTpl = "blank_columns.wap.tpl";

		viewAction.project.openFile(projectName, "docroot", layoutWapTpl);

		editorAction.close();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createLayoutTemplateProject() {
		wizardAction.openNewLiferayPluginProjectWizard();

		String projectName = "test-layouttpl";

		wizardAction.newPlugin.prepareLayoutTemplateSdk(projectName);

		wizardAction.finish();

		jobAction.waitForIvy();

		jobAction.waitForValidate(projectName);

		viewAction.project.closeAndDelete(projectName);
	}

}