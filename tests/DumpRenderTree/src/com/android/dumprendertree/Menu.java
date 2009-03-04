/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.dumprendertree;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;

public class Menu extends FileList {
    
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
    }
    
    boolean fileFilter(File f) {
    	if (f.getName().startsWith("."))
    		return false;
    	if (f.getName().equalsIgnoreCase("resources"))
    		return false;
    	if (f.isDirectory())
    		return true;
    	if (f.getPath().toLowerCase().endsWith("ml"))
    		return true;
    	return false;
    }
    
    void processFile(String filename, boolean selection)
    {
        Intent result = new Intent();
        result.setClass(this, HTMLHostActivity.class);
        result.putExtra(HTMLHostActivity.RESUME_FROM_CRASH, false);
        result.putExtra(HTMLHostActivity.SINGLE_TEST_MODE, true);
        result.putExtra(HTMLHostActivity.TEST_PATH_PREFIX, filename);
        result.putExtra(HTMLHostActivity.TIMEOUT_IN_MILLIS, 8000);
        startActivity(result);
    }
   
}

