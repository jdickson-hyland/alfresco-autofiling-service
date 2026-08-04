/*
 * Copyright 2026 Hyland Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.autofiling.model;

import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class AutofilingRuleTest {

    @Test
    public void fromJsonPopulatesAllFields() {
        String json = "{"
            + "\"name\":\"Invoice Rule\","
            + "\"description\":\"Files invoices\","
            + "\"enabled\":true,"
            + "\"inboxPath\":\"/Company Home/Inbox\","
            + "\"contentTypes\":[\"acme:invoice\",\"acme:creditNote\"],"
            + "\"pathTemplate\":\"/Company Home/Finance/{acme:year}\","
            + "\"createMissingFolders\":true,"
            + "\"priority\":5"
            + "}";

        AutofilingRule rule = AutofilingRule.fromJson("workspace://SpacesStore/abc", json);

        assertEquals("workspace://SpacesStore/abc", rule.getNodeRef());
        assertEquals("Invoice Rule", rule.getName());
        assertEquals("Files invoices", rule.getDescription());
        assertTrue(rule.isEnabled());
        assertEquals("/Company Home/Inbox", rule.getInboxPath());
        assertEquals(Arrays.asList("acme:invoice", "acme:creditNote"), rule.getContentTypes());
        assertEquals("/Company Home/Finance/{acme:year}", rule.getPathTemplate());
        assertTrue(rule.isCreateMissingFolders());
        assertEquals(5, rule.getPriority());
    }

    @Test
    public void fromJsonAppliesDefaults() {
        String json = "{\"name\":\"Minimal Rule\"}";
        AutofilingRule rule = AutofilingRule.fromJson(null, json);

        assertTrue("enabled defaults to true", rule.isEnabled());
        assertEquals("priority defaults to 10", 10, rule.getPriority());
        assertTrue("createMissingFolders defaults to true", rule.isCreateMissingFolders());
        assertNotNull("contentTypes never null", rule.getContentTypes());
        assertTrue("contentTypes defaults to empty", rule.getContentTypes().isEmpty());
        assertEquals("", rule.getInboxPath());
        assertEquals("", rule.getPathTemplate());
    }

    @Test
    public void toJsonRoundTrip() {
        AutofilingRule original = new AutofilingRule();
        original.setNodeRef("workspace://SpacesStore/test");
        original.setName("Round Trip Rule");
        original.setDescription("Test description");
        original.setEnabled(false);
        original.setInboxPath("/Company Home/Inbox");
        original.setContentTypes(Arrays.asList("cm:content", "acme:doc"));
        original.setPathTemplate("/Company Home/{cm:created|yyyy}");
        original.setCreateMissingFolders(false);
        original.setPriority(20);

        String json = original.toJson().toString();
        AutofilingRule roundTripped = AutofilingRule.fromJson(original.getNodeRef(), json);

        assertEquals(original.getNodeRef(), roundTripped.getNodeRef());
        assertEquals(original.getName(), roundTripped.getName());
        assertEquals(original.getDescription(), roundTripped.getDescription());
        assertEquals(original.isEnabled(), roundTripped.isEnabled());
        assertEquals(original.getInboxPath(), roundTripped.getInboxPath());
        assertEquals(original.getContentTypes(), roundTripped.getContentTypes());
        assertEquals(original.getPathTemplate(), roundTripped.getPathTemplate());
        assertEquals(original.isCreateMissingFolders(), roundTripped.isCreateMissingFolders());
        assertEquals(original.getPriority(), roundTripped.getPriority());
    }

    @Test
    public void toJsonIncludesNodeRefField() {
        AutofilingRule rule = new AutofilingRule();
        rule.setNodeRef("workspace://SpacesStore/xyz");
        rule.setName("Test");

        JSONObject json = rule.toJson();
        assertEquals("workspace://SpacesStore/xyz", json.getString("nodeRef"));
    }

    @Test
    public void fromJsonHandlesEmptyContentTypes() {
        String json = "{\"name\":\"Rule\",\"contentTypes\":[]}";
        AutofilingRule rule = AutofilingRule.fromJson(null, json);

        assertNotNull(rule.getContentTypes());
        assertTrue(rule.getContentTypes().isEmpty());
    }

    @Test
    public void fromJsonHandlesMissingContentTypesKey() {
        String json = "{\"name\":\"Rule\"}";
        AutofilingRule rule = AutofilingRule.fromJson(null, json);

        assertNotNull(rule.getContentTypes());
        assertTrue(rule.getContentTypes().isEmpty());
    }

    @Test
    public void disabledFlagPreserved() {
        String json = "{\"name\":\"Rule\",\"enabled\":false}";
        AutofilingRule rule = AutofilingRule.fromJson(null, json);
        assertFalse(rule.isEnabled());

        String serialised = rule.toJson().toString();
        AutofilingRule roundTripped = AutofilingRule.fromJson(null, serialised);
        assertFalse(roundTripped.isEnabled());
    }
}
