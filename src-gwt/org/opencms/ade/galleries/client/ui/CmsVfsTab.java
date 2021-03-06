/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsVfsTabHandler;
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.category.CmsDataValue;
import org.opencms.gwt.client.ui.tree.A_CmsLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.shared.CmsIconUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The tab widget for selecting folders from the VFS tree.<p>
 * 
 * @since 8.0.0
 */
public class CmsVfsTab extends A_CmsListTab {

    /** 
     * Handles the change of the item selection.<p>
     */
    private class SelectionHandler extends A_SelectionHandler {

        /** The category path as id for the selected category. */
        private CmsVfsEntryBean m_vfsEntry;

        /**
         * Constructor.<p>
         * 
         * @param vfsEntry the vfs entry represented by the list item
         * @param checkBox the reference to the checkbox
         */
        public SelectionHandler(CmsVfsEntryBean vfsEntry, CmsCheckBox checkBox) {

            super(checkBox);
            m_vfsEntry = vfsEntry;
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#onDoubleClick(com.google.gwt.event.dom.client.DoubleClickEvent)
         */
        @Override
        public void onDoubleClick(DoubleClickEvent event) {

            if (isIncludeFiles()) {
                super.onDoubleClick(event);
            } else if (getTabHandler().hasSelectResource()) {
                String selectPath = m_tabHandler.getSelectPath(m_vfsEntry);
                getTabHandler().selectResource(
                    selectPath,
                    m_vfsEntry.getStructureId(),
                    m_vfsEntry.getDisplayName(),
                    I_CmsGalleryProviderConstants.RESOURCE_TYPE_FOLDER);
            }
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#onSelectionChange()
         */
        @Override
        protected void onSelectionChange() {

            if (isIncludeFiles()) {
                getTabHandler().onSelectFolder(m_vfsEntry.getRootPath(), getCheckBox().isChecked());
            }
        }
    }

    /** Text metrics key. */
    private static final String TM_VFS_TAB = "VfsTab";

    /** A map from tree items to the corresponding data beans. */
    protected IdentityHashMap<CmsLazyTreeItem, CmsVfsEntryBean> m_entryMap = new IdentityHashMap<CmsLazyTreeItem, CmsVfsEntryBean>();

    /** The tab handler. */
    protected CmsVfsTabHandler m_tabHandler;

    /** Flag indicating files are included. */
    private boolean m_includeFiles;

    /** A map of tree items indexed by VFS path. */
    private Map<String, CmsLazyTreeItem> m_itemsByPath = new HashMap<String, CmsLazyTreeItem>();

    /**
     * Constructor.<p>
     * 
     * @param tabHandler the tab handler 
     * @param includeFiles the include files flag
     */
    public CmsVfsTab(CmsVfsTabHandler tabHandler, boolean includeFiles) {

        super(GalleryTabId.cms_tab_vfstree);
        m_scrollList.truncate(TM_VFS_TAB, CmsGalleryDialog.DIALOG_WIDTH);
        m_tabHandler = tabHandler;
        m_includeFiles = includeFiles;
        init();
    }

    /**
     * Sets the initial folders in the VFS tab.<p>
     * 
     * @param entries the root folders to display 
     */
    public void fillInitially(List<CmsVfsEntryBean> entries) {

        clear();
        for (CmsVfsEntryBean entry : entries) {
            CmsLazyTreeItem item = createItem(entry);
            addWidgetToList(item);
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanels(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public List<CmsSearchParamPanel> getParamPanels(CmsGallerySearchBean searchObj) {

        List<CmsSearchParamPanel> result = new ArrayList<CmsSearchParamPanel>();
        for (String folder : searchObj.getFolders()) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_PARAMS_LABEL_FOLDERS_0),
                this);
            panel.setContent(folder, folder);
            result.add(panel);
        }
        return result;
    }

    /**
     * Un-checks the check boxes for each folder passed in the <code>folders</code> parameter.<p>
     * 
     * @param folders the folders for which the check boxes should be unchecked 
     */
    public void uncheckFolders(Collection<String> folders) {

        for (String folder : folders) {
            CmsLazyTreeItem item = m_itemsByPath.get(folder);
            if (item != null) {
                item.getCheckBox().setChecked(false);
            }
        }
    }

    /**
     * Clears the contents of the tab and resets the mapping from tree items to VFS beans.<p>
     */
    protected void clear() {

        clearList();
        m_entryMap = new IdentityHashMap<CmsLazyTreeItem, CmsVfsEntryBean>();
    }

    /**
     * Helper method for creating a VFS tree item widget from a VFS entry bean.<p>
     * 
     * @param vfsEntry the VFS entry bean 
     * 
     * @return the tree item widget
     */
    protected CmsLazyTreeItem createItem(final CmsVfsEntryBean vfsEntry) {

        String name = null;
        String rootPath = vfsEntry.getRootPath();
        if (rootPath.equals("/") || rootPath.equals("")) {
            name = "/";
        } else {
            name = CmsResource.getName(vfsEntry.getRootPath());

            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }
        }
        CmsDataValue dataValue = new CmsDataValue(600, 3, CmsIconUtil.getResourceIconClasses(
            I_CmsGalleryProviderConstants.RESOURCE_TYPE_FOLDER,
            true), name, vfsEntry.getDisplayName());
        dataValue.setUnselectable();
        if (vfsEntry.isEditable()) {
            dataValue.addButton(createUploadButtonForTarget(vfsEntry.getRootPath(), true));
        }
        CmsLazyTreeItem result;
        SelectionHandler selectionHandler;
        if (isIncludeFiles()) {
            final CmsCheckBox checkbox = new CmsCheckBox();
            result = new CmsLazyTreeItem(checkbox, dataValue, true);
            selectionHandler = new SelectionHandler(vfsEntry, checkbox);
            checkbox.addClickHandler(selectionHandler);
            dataValue.addButton(createSelectButton(selectionHandler));
        } else {
            result = new CmsLazyTreeItem(dataValue, true);
            selectionHandler = new SelectionHandler(vfsEntry, null);
        }
        dataValue.addDomHandler(selectionHandler, DoubleClickEvent.getType());
        if (getTabHandler().hasSelectResource()) {
            String selectPath = m_tabHandler.getSelectPath(vfsEntry);
            dataValue.addButton(createSelectResourceButton(
                selectPath,
                vfsEntry.getStructureId(),
                vfsEntry.getDisplayName(),
                I_CmsGalleryProviderConstants.RESOURCE_TYPE_FOLDER));
        }
        m_entryMap.put(result, vfsEntry);
        m_itemsByPath.put(vfsEntry.getRootPath(), result);
        result.setLeafStyle(false);
        result.setSmallView(true);
        return result;

    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#createScrollList()
     */
    @Override
    protected CmsList<? extends I_CmsListItem> createScrollList() {

        return new CmsLazyTree<CmsLazyTreeItem>(new A_CmsLazyOpenHandler<CmsLazyTreeItem>() {

            /**
             * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
             */
            public void load(final CmsLazyTreeItem target) {

                CmsVfsEntryBean entry = m_entryMap.get(target);
                String path = entry.getRootPath();
                AsyncCallback<List<CmsVfsEntryBean>> callback = new AsyncCallback<List<CmsVfsEntryBean>>() {

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
                     */
                    public void onFailure(Throwable caught) {

                        // should never be called 

                    }

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
                     */
                    public void onSuccess(List<CmsVfsEntryBean> result) {

                        for (CmsVfsEntryBean childEntry : result) {
                            CmsLazyTreeItem item = createItem(childEntry);
                            target.addChild(item);
                        }
                        target.onFinishLoading();
                    }
                };

                m_tabHandler.getSubFolders(path, callback);

            }
        });
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected LinkedHashMap<String, String> getSortList() {

        return m_tabHandler.getSortList();
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    protected CmsVfsTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * Returns if files are included.<p>
     * 
     * @return <code>true</code> if files are included
     */
    protected boolean isIncludeFiles() {

        return m_includeFiles;
    }

}
