/* ------------------------------------------------------------------------
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
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
 * ------------------------------------------------------------------------
 */

package us.terebi.net.telnet;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class TelnetCodes
{

    static final byte IAC = (byte) 255;
    static final byte SE = (byte) 240;
    static final byte NOP = (byte) 241;
    static final byte DM = (byte) 242;
    static final byte BRK = (byte) 243;
    static final byte IP = (byte) 244;
    static final byte AO = (byte) 245;
    static final byte AYT = (byte) 246;
    static final byte EC = (byte) 247;
    static final byte EL = (byte) 248;
    static final byte GA = (byte) 249;
    static final byte SB = (byte) 250;
    static final byte WILL = (byte) 251;
    static final byte WONT = (byte) 252;
    static final byte DO = (byte) 253;
    static final byte DONT = (byte) 254;
    
    static final byte OPT_SERVER_ECHO = 1;

}
