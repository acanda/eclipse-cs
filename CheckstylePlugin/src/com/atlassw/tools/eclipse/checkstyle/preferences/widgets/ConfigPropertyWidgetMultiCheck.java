//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars K�dderitzsch
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package com.atlassw.tools.eclipse.checkstyle.preferences.widgets;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;

/**
 * Configuration widget for selecting multiple values with check boxes.
 */
public class ConfigPropertyWidgetMultiCheck extends ConfigPropertyWidgetAbstractBase implements
        IPropertyChangeListener
{
    //=================================================
    // Public static final variables.
    //=================================================

    /** Resource bundle containing the token translations. */
    private static final ResourceBundle TOKEN_BUNDLE = PropertyResourceBundle
            .getBundle("com.atlassw.tools.eclipse.checkstyle.preferences.token"); //$NON-NLS-1$

    //=================================================
    // Static class variables.
    //=================================================

    //=================================================
    // Instance member variables.
    //=================================================

    private CheckboxTableViewer mTable;

    private boolean mTranslateTokens;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Creates the widget.
     * 
     * @param parent the parent composite
     * @param prop the property
     */
    public ConfigPropertyWidgetMultiCheck(Composite parent, ConfigProperty prop)
    {
        super(parent, prop);
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @see ConfigPropertyWidgetAbstractBase#getValueWidget(org.eclipse.swt.widgets.Composite)
     */
    protected Control getValueWidget(Composite parent)
    {
        if (mTable == null)
        {
            IPreferenceStore prefStore = CheckstylePlugin.getDefault().getPreferenceStore();
            mTranslateTokens = prefStore.getBoolean(CheckstylePlugin.PREF_TRANSLATE_TOKENS);
            prefStore.addPropertyChangeListener(this);

            mTable = CheckboxTableViewer.newCheckList(parent, SWT.V_SCROLL | SWT.BORDER);
            mTable.setContentProvider(new ArrayContentProvider());
            mTable.setLabelProvider(new TokenLabelProvider());
            mTable.setInput(getConfigProperty().getMetaData().getPropertyEnumeration());
            mTable.setCheckedElements(getInitialValues().toArray());

            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.heightHint = 150;
            mTable.getControl().setLayoutData(gd);

            //deregister the listener on widget dipose
            mTable.getControl().addDisposeListener(new DisposeListener()
            {

                public void widgetDisposed(DisposeEvent e)
                {
                    IPreferenceStore prefStore = CheckstylePlugin.getDefault().getPreferenceStore();
                    prefStore.removePropertyChangeListener(ConfigPropertyWidgetMultiCheck.this);
                }
            });
        }

        return mTable.getControl();
    }

    /**
     * {@inheritDoc}
     */
    public String getValue()
    {
        StringBuffer buffer = new StringBuffer(""); //$NON-NLS-1$

        Object[] checkedElements = mTable.getCheckedElements();

        for (int i = 0; i < checkedElements.length; i++)
        {

            if (i > 0)
            {
                buffer.append(","); //$NON-NLS-1$
            }
            buffer.append(checkedElements[i]);
        }
        return buffer.toString();
    }

    private List getInitialValues()
    {
        List result = new LinkedList();
        StringTokenizer tokenizer = new StringTokenizer(getInitValue(), ","); //$NON-NLS-1$
        while (tokenizer.hasMoreTokens())
        {
            result.add(tokenizer.nextToken().trim());
        }

        return result;
    }

    /**
     * @see ConfigPropertyWidgetAbstractBase#restorePropertyDefault()
     */
    public void restorePropertyDefault()
    {
        String defaultValue = getConfigProperty().getMetaData().getDefaultValue();
        List result = new LinkedList();

        if (defaultValue != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(defaultValue, ","); //$NON-NLS-1$
            while (tokenizer.hasMoreTokens())
            {
                result.add(tokenizer.nextToken().trim());
            }
        }

        //clear current checked state
        mTable.setCheckedElements(new Object[0]);

        mTable.setCheckedElements(result.toArray());
    }

    /**
     * Label provider to translate checkstyle tokens into readable form.
     * 
     * @author Lars K�dderitzsch
     */
    private class TokenLabelProvider extends LabelProvider
    {

        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element)
        {
            String translation = null;
            if (!mTranslateTokens)
            {
                translation = "" + element; //$NON-NLS-1$
            }
            else
            {
                try
                {
                    translation = TOKEN_BUNDLE.getString((String) element);
                }
                catch (MissingResourceException e)
                {
                    translation = "" + element; //$NON-NLS-1$
                }
            }
            return translation;
        }

    }

    /**
     * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {

        if (CheckstylePlugin.PREF_TRANSLATE_TOKENS.equals(event.getProperty()))
        {
            mTranslateTokens = ((Boolean) event.getNewValue()).booleanValue();
            mTable.refresh(true);
        }
    }
}