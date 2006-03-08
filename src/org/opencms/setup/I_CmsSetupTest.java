/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/I_CmsSetupTest.java,v $
 * Date   : $Date: 2006/03/08 15:05:50 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup;

/**
 * Represent a test to give users infos about whether their system is compatible to OpenCms.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.8 
 */
public interface I_CmsSetupTest {

    /** Test failed display text. */
    public static final String RESULT_FAILED = "failed!";

    /** Test passed display text. */
    public static final String RESULT_PASSED = "passed";

    /** Test warning display text. */
    public static final String RESULT_WARNING = "warning!";

    /**
     * Returns the nice name for the test.<p>
     * 
     * @return the nice name
     */
    public String getName();

    /**
     * Returns the test results.<p>
     * 
     * @param setupBean the setup bean
     * 
     * @return the test results
     * 
     * @throws Exception if something goes wrong 
     */
    public CmsSetupTestResult run(CmsSetupBean setupBean) throws Exception;
}
