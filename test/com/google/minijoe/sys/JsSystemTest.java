// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import com.google.common.util.text.TextUtilTest;
import com.google.test.GoogleTestCase;

import j2meunit.framework.Test;
import j2meunit.framework.TestSuite;

public class JsSystemTest extends GoogleTestCase {

  public void testExp(){
    assertEquals(1, JsSystem.exp(0), 0.00000);
    assertEquals(2.71828183, JsSystem.exp(1), 0.00000001);
    assertEquals(22026.4658, JsSystem.exp(10), 0.0001);
    assertEquals(0.367879441, JsSystem.exp(-1), 0.00000001);
  }

  public void testLn() {
    assertEquals(0.0, JsSystem.ln(1), 0.0000001);
    assertEquals(1.0, JsSystem.ln(Math.E), 0.0000001);
  }

  public void testAvg() {
    assertEquals(13.45817148, JsSystem.avg(24, 6), 0.00001);
  }

  public void testFormatNumber (){
    assertEquals("12.35", JsSystem.formatNumber(JsObject.ID_TO_FIXED, 12.3456789, 2));
    assertEquals("12000.00", JsSystem.formatNumber(JsObject.ID_TO_FIXED, 12000, 2));
    assertEquals("0.01", JsSystem.formatNumber(JsObject.ID_TO_FIXED, 0.012345, 2));
    assertEquals("0.0", JsSystem.formatNumber(JsObject.ID_TO_FIXED, 0.000012345, 1));
  }
  
  public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new TextUtilTest("testExp") {
      public void runTest() {
        testExp();
      }
    });
    suite.addTest(new TextUtilTest("testLn") {
      public void runTest() {
        testLn();
      }
    });
    suite.addTest(new TextUtilTest("testAvg") {
      public void runTest() {
        testAvg();
      }
    });
    suite.addTest(new TextUtilTest("testFormatNumber") {
      public void runTest() {
        testFormatNumber();
      }
    });
    return suite;
  }
}
