/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.dso.editors;

import org.apache.xmlbeans.XmlObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.terracotta.dso.editors.chooser.ExpressionChooser;
import org.terracotta.dso.editors.chooser.MethodBehavior;
import org.terracotta.dso.editors.chooser.NavigatorBehavior;
import org.terracotta.dso.editors.xmlbeans.XmlConfigContext;
import org.terracotta.dso.editors.xmlbeans.XmlConfigEvent;
import org.terracotta.dso.editors.xmlbeans.XmlConfigUndoContext;
import org.terracotta.ui.util.SWTUtil;

import com.tc.util.event.UpdateEvent;
import com.tc.util.event.UpdateEventListener;
import com.terracottatech.config.DistributedMethods;

public class DistributedMethodsPanel extends ConfigurationEditorPanel {

  private final Layout m_layout;
  private State        m_state;

  public DistributedMethodsPanel(Composite parent, int style) {
    super(parent, style);
    this.m_layout = new Layout(this);
  }

  // ================================================================================
  // INTERFACE
  // ================================================================================

  public synchronized void clearState() {
    setActive(false);
    m_layout.reset();
    m_state.xmlContext.detachComponentModel(this);
    m_state = null;
  }

  public synchronized void init(Object data) {
    if (m_isActive && m_state.project == (IProject) data) return;
    setActive(false);
    m_state = new State((IProject) data);
    createContextListeners();
    initTableItems();
    setActive(true);
  }

  public synchronized void refreshContent() {
    m_layout.reset();
    initTableItems();
  }
  
  public void detach() {
    m_state.xmlContext.detachComponentModel(this);
  }
  
  // ================================================================================
  // INIT LISTENERS
  // ================================================================================

  private void createContextListeners() {
    m_state.xmlContext.detachComponentModel(this); // clear existing
    m_layout.m_table.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (!m_isActive) return;
        m_layout.m_removeButton.setEnabled(true);
      }
    });
    m_layout.m_table.addListener(SWT.SetData, new Listener() {
      public void handleEvent(Event e) {
        if (!m_isActive) return;
        TableItem item = (TableItem) e.item;
        DistributedMethods.MethodExpression expr = (DistributedMethods.MethodExpression) item.getData();
        XmlConfigEvent event = new XmlConfigEvent(item.getText(e.index), null, expr, XmlConfigEvent.DISTRIBUTED_METHOD);
        event.index = m_layout.m_table.getSelectionIndex();
        m_state.xmlContext.notifyListeners(event);
      }
    });
    m_state.xmlContext.addListener(new UpdateEventListener() {
      public void handleUpdate(UpdateEvent e) {
        if (!m_isActive) return;
        XmlConfigEvent event = castEvent(e);
        if (event.data == null) return;
        XmlObject method = event.element;
        TableItem[] items = m_layout.m_table.getItems();
        for (int i = 0; i < items.length; i++) {
          if (items[i].getData() == method) {
            items[i].setText((String) event.data);
          }
        }
      }
    }, XmlConfigEvent.DISTRIBUTED_METHOD, this);
    // - remove method
    m_layout.m_removeButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (!m_isActive) return;
        setActive(false);
        m_layout.m_table.forceFocus();
        int selected = m_layout.m_table.getSelectionIndex();
        XmlConfigEvent event = new XmlConfigEvent(XmlConfigEvent.DELETE_DISTRIBUTED_METHOD);
        event.index = selected;
        setActive(true);
        m_state.xmlContext.notifyListeners(event);
      }
    });
    m_state.xmlContext.addListener(new UpdateEventListener() {
      public void handleUpdate(UpdateEvent e) {
        if (!m_isActive) return;
        m_layout.m_table.remove(((XmlConfigEvent) e).index);
        m_layout.m_removeButton.setEnabled(false);
      }
    }, XmlConfigEvent.REMOVE_DISTRIBUTED_METHOD, this);
    // - add method
    m_layout.m_addButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (!m_isActive) return;
        setActive(false);
        m_layout.m_table.forceFocus();
        NavigatorBehavior behavior = new MethodBehavior();
        ExpressionChooser chooser = new ExpressionChooser(getShell(), behavior.getTitle(), MethodBehavior.ADD_MSG,
            m_state.project, behavior);
        chooser.addValueListener(new UpdateEventListener() {
          public void handleUpdate(UpdateEvent updateEvent) {
            String[] items = (String[]) updateEvent.data;
            for (int i = 0; i < items.length; i++) {
              XmlConfigEvent event = new XmlConfigEvent(XmlConfigEvent.CREATE_DISTRIBUTED_METHOD);
              event.data = items[i];
              m_state.xmlContext.notifyListeners(event);
            }
          }
        });
        setActive(true);
        chooser.open();
      }
    });
    m_state.xmlContext.addListener(new UpdateEventListener() {
      public void handleUpdate(UpdateEvent e) {
        if (!m_isActive) return;
        XmlConfigEvent event = castEvent(e);
        String value = ((DistributedMethods.MethodExpression) event.element).getStringValue();
        createTableItem((DistributedMethods.MethodExpression) event.element, value);
      }
    }, XmlConfigEvent.NEW_DISTRIBUTED_METHOD, this);
  }

  // ================================================================================
  // HELPERS
  // ================================================================================

  private void initTableItems() {
    DistributedMethods distributedMethods = m_state.xmlContext.getParentElementProvider().hasDistributedMethods();
    if (distributedMethods == null) return;
    DistributedMethods.MethodExpression[] methods = distributedMethods.getMethodExpressionArray();
    for (int i = 0; i < methods.length; i++) {
      createTableItem(methods[i], methods[i].getStringValue());
    }
  }

  private void createTableItem(DistributedMethods.MethodExpression method, String element) {
    TableItem item = new TableItem(m_layout.m_table, SWT.NONE);
    item.setText(element);
    item.setData(method);
  }

  // ================================================================================
  // STATE
  // ================================================================================

  private class State {
    final IProject             project;
    final XmlConfigContext     xmlContext;
    final XmlConfigUndoContext xmlUndoContext;

    private State(IProject project) {
      this.project = project;
      this.xmlContext = XmlConfigContext.getInstance(project);
      this.xmlUndoContext = XmlConfigUndoContext.getInstance(project);
    }
  }

  // ================================================================================
  // LAYOUT
  // ================================================================================

  private class Layout {

    private static final String DISTRIBUTED_METHODS = "Distributed Methods";
    private static final String ADD                 = "Add...";
    private static final String REMOVE              = "Remove";

    private Button              m_addButton;
    private Button              m_removeButton;
    private Table               m_table;

    public void reset() {
      m_removeButton.setEnabled(false);
      m_table.removeAll();
    }

    private Layout(Composite parent) {
      Composite comp = new Composite(parent, SWT.NONE);
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      gridLayout.marginWidth = 10;
      gridLayout.marginHeight = 10;
      gridLayout.makeColumnsEqualWidth = false;
      comp.setLayout(gridLayout);

      Composite sidePanel = new Composite(comp, SWT.NONE);
      gridLayout = new GridLayout();
      gridLayout.numColumns = 1;
      gridLayout.marginWidth = 0;
      gridLayout.marginHeight = 0;
      sidePanel.setLayout(gridLayout);
      sidePanel.setLayoutData(new GridData(GridData.FILL_BOTH));

      Label label = new Label(sidePanel, SWT.NONE);
      label.setText(DISTRIBUTED_METHODS);
      label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

      Composite tablePanel = new Composite(sidePanel, SWT.BORDER);
      tablePanel.setLayout(new FillLayout());
      tablePanel.setLayoutData(new GridData(GridData.FILL_BOTH));
      m_table = new Table(tablePanel, SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL);
      m_table.setHeaderVisible(false);
      m_table.setLinesVisible(true);
      SWTUtil.makeTableColumnsEditable(m_table, new int[] { 0 });

      TableColumn column = new TableColumn(m_table, SWT.NONE);
      column.setText(DISTRIBUTED_METHODS);
      column.pack();

      Composite buttonPanel = new Composite(comp, SWT.NONE);
      gridLayout = new GridLayout();
      gridLayout.numColumns = 1;
      gridLayout.marginWidth = 0;
      gridLayout.marginHeight = 0;
      buttonPanel.setLayout(gridLayout);
      buttonPanel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

      new Label(buttonPanel, SWT.NONE); // filler

      m_addButton = new Button(buttonPanel, SWT.PUSH);
      m_addButton.setText(ADD);
      m_addButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
      SWTUtil.applyDefaultButtonSize(m_addButton);

      m_removeButton = new Button(buttonPanel, SWT.PUSH);
      m_removeButton.setText(REMOVE);
      m_removeButton.setEnabled(false);
      m_removeButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
      SWTUtil.applyDefaultButtonSize(m_removeButton);
    }
  }
}
