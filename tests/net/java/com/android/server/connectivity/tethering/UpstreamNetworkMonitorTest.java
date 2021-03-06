/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.connectivity.tethering;

import static android.net.ConnectivityManager.TYPE_MOBILE_DUN;
import static android.net.ConnectivityManager.TYPE_MOBILE_HIPRI;
import static android.net.NetworkCapabilities.NET_CAPABILITY_DUN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.IConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class UpstreamNetworkMonitorTest {
    private static final int EVENT_UNM_UPDATE = 1;

    @Mock private Context mContext;
    @Mock private IConnectivityManager mCS;

    private TestConnectivityManager mCM;
    private UpstreamNetworkMonitor mUNM;

    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        reset(mContext);
        reset(mCS);

        mCM = new TestConnectivityManager(mContext, mCS);
        mUNM = new UpstreamNetworkMonitor(null, EVENT_UNM_UPDATE, (ConnectivityManager) mCM);
    }

    @Test
    public void testDoesNothingBeforeStarted() {
        assertTrue(mCM.hasNoCallbacks());
        assertFalse(mUNM.mobileNetworkRequested());

        mUNM.updateMobileRequiresDun(true);
        assertTrue(mCM.hasNoCallbacks());
        mUNM.updateMobileRequiresDun(false);
        assertTrue(mCM.hasNoCallbacks());
    }

    @Test
    public void testDefaultNetworkIsTracked() throws Exception {
        assertEquals(0, mCM.trackingDefault.size());

        mUNM.start();
        assertEquals(1, mCM.trackingDefault.size());

        mUNM.stop();
        assertTrue(mCM.hasNoCallbacks());
    }

    @Test
    public void testListensForAllNetworks() throws Exception {
        assertTrue(mCM.listening.isEmpty());

        mUNM.start();
        assertFalse(mCM.listening.isEmpty());
        assertTrue(mCM.isListeningForAll());

        mUNM.stop();
        assertTrue(mCM.hasNoCallbacks());
    }

    @Test
    public void testRequestsMobileNetwork() throws Exception {
        assertFalse(mUNM.mobileNetworkRequested());
        assertEquals(0, mCM.requested.size());

        mUNM.start();
        assertFalse(mUNM.mobileNetworkRequested());
        assertEquals(0, mCM.requested.size());

        mUNM.updateMobileRequiresDun(false);
        assertFalse(mUNM.mobileNetworkRequested());
        assertEquals(0, mCM.requested.size());

        mUNM.registerMobileNetworkRequest();
        assertTrue(mUNM.mobileNetworkRequested());
        assertUpstreamTypeRequested(TYPE_MOBILE_HIPRI);
        assertFalse(mCM.isDunRequested());

        mUNM.stop();
        assertFalse(mUNM.mobileNetworkRequested());
        assertTrue(mCM.hasNoCallbacks());
    }

    @Test
    public void testRequestsDunNetwork() throws Exception {
        assertFalse(mUNM.mobileNetworkRequested());
        assertEquals(0, mCM.requested.size());

        mUNM.start();
        assertFalse(mUNM.mobileNetworkRequested());
        assertEquals(0, mCM.requested.size());

        mUNM.updateMobileRequiresDun(true);
        assertFalse(mUNM.mobileNetworkRequested());
        assertEquals(0, mCM.requested.size());

        mUNM.registerMobileNetworkRequest();
        assertTrue(mUNM.mobileNetworkRequested());
        assertUpstreamTypeRequested(TYPE_MOBILE_DUN);
        assertTrue(mCM.isDunRequested());

        mUNM.stop();
        assertFalse(mUNM.mobileNetworkRequested());
        assertTrue(mCM.hasNoCallbacks());
    }

    @Test
    public void testUpdateMobileRequiredDun() throws Exception {
        mUNM.start();

        // Test going from no-DUN to DUN correctly re-registers callbacks.
        mUNM.updateMobileRequiresDun(false);
        mUNM.registerMobileNetworkRequest();
        assertTrue(mUNM.mobileNetworkRequested());
        assertUpstreamTypeRequested(TYPE_MOBILE_HIPRI);
        assertFalse(mCM.isDunRequested());
        mUNM.updateMobileRequiresDun(true);
        assertTrue(mUNM.mobileNetworkRequested());
        assertUpstreamTypeRequested(TYPE_MOBILE_DUN);
        assertTrue(mCM.isDunRequested());

        // Test going from DUN to no-DUN correctly re-registers callbacks.
        mUNM.updateMobileRequiresDun(false);
        assertTrue(mUNM.mobileNetworkRequested());
        assertUpstreamTypeRequested(TYPE_MOBILE_HIPRI);
        assertFalse(mCM.isDunRequested());

        mUNM.stop();
        assertFalse(mUNM.mobileNetworkRequested());
    }

    private void assertUpstreamTypeRequested(int upstreamType) throws Exception {
        assertEquals(1, mCM.requested.size());
        assertEquals(1, mCM.legacyTypeMap.size());
        assertEquals(Integer.valueOf(upstreamType),
                mCM.legacyTypeMap.values().iterator().next());
    }

    private static class TestConnectivityManager extends ConnectivityManager {
        public Set<NetworkCallback> trackingDefault = new HashSet<>();
        public Map<NetworkCallback, NetworkRequest> listening = new HashMap<>();
        public Map<NetworkCallback, NetworkRequest> requested = new HashMap<>();
        public Map<NetworkCallback, Integer> legacyTypeMap = new HashMap<>();

        public TestConnectivityManager(Context ctx, IConnectivityManager svc) {
            super(ctx, svc);
        }

        boolean hasNoCallbacks() {
            return trackingDefault.isEmpty() &&
                   listening.isEmpty() &&
                   requested.isEmpty() &&
                   legacyTypeMap.isEmpty();
        }

        boolean isListeningForAll() {
            final NetworkCapabilities empty = new NetworkCapabilities();
            empty.clearAll();

            for (NetworkRequest req : listening.values()) {
                if (req.networkCapabilities.equalRequestableCapabilities(empty)) {
                    return true;
                }
            }
            return false;
        }

        boolean isDunRequested() {
            for (NetworkRequest req : requested.values()) {
                if (req.networkCapabilities.hasCapability(NET_CAPABILITY_DUN)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void requestNetwork(NetworkRequest req, NetworkCallback cb) {
            assertFalse(requested.containsKey(cb));
            requested.put(cb, req);
        }

        @Override
        public void requestNetwork(NetworkRequest req, NetworkCallback cb,
                int timeoutMs, int legacyType) {
            assertFalse(requested.containsKey(cb));
            requested.put(cb, req);
            assertFalse(legacyTypeMap.containsKey(cb));
            if (legacyType != ConnectivityManager.TYPE_NONE) {
                legacyTypeMap.put(cb, legacyType);
            }
        }

        @Override
        public void registerNetworkCallback(NetworkRequest req, NetworkCallback cb) {
            assertFalse(listening.containsKey(cb));
            listening.put(cb, req);
        }

        @Override
        public void registerDefaultNetworkCallback(NetworkCallback cb) {
            assertFalse(trackingDefault.contains(cb));
            trackingDefault.add(cb);
        }

        @Override
        public void unregisterNetworkCallback(NetworkCallback cb) {
            if (trackingDefault.contains(cb)) {
                trackingDefault.remove(cb);
            } else if (listening.containsKey(cb)) {
                listening.remove(cb);
            } else if (requested.containsKey(cb)) {
                requested.remove(cb);
                legacyTypeMap.remove(cb);
            } else {
                fail("Unexpected callback removed");
            }

            assertFalse(trackingDefault.contains(cb));
            assertFalse(listening.containsKey(cb));
            assertFalse(requested.containsKey(cb));
        }
    }
}
