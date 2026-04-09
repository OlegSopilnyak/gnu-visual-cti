/*
##############################################################################
##
##  DO NOT REMOVE THIS LICENSE AND COPYRIGHT NOTICE FOR ANY REASON
##
##############################################################################

GNU VisualCTI - A Java multi-platform Computer Telephony Application Server
Copyright (C) 2002 by Oleg Sopilnyak.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Contact oleg.sopilnyak@gmail.com or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg.sopilnyak@gmail.com
Home Phone:	+380-63-8420220 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.briquette;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.plaf.ColorUIResource;

import org.visualcti.briquette.core.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: Parameters set visualization</p>
 * <p>Copyright: Copyright (c) Prominic Inc & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public class ParametersSetUI extends JPanel {
/**
 * <attribute>
 * The tree of parameters
 * */
private final JTree tree;
/**
 * <attribute>
 * The Tree's model
 * */
private final DefaultTreeModel treeModel;
/**
 * <attribute>
 * The root of tree
 * */
private final DefaultMutableTreeNode root;
/**
 * <attribute>
 * The entry of input parameters
 * */
private final DefaultMutableTreeNode input;
/**
 * <attribute>
 * The entry of output parameters
 * */
private final DefaultMutableTreeNode output;
  /**
   * <constructor>
   * */
  public ParametersSetUI() {
    super( new BorderLayout(), false );
    super.setBorder( new BevelBorder(BevelBorder.LOWERED) );
    this.root = new DefaultMutableTreeNode("parameters");
    this.input = new DefaultMutableTreeNode("input");
    this.output = new DefaultMutableTreeNode("output");
    this.root.add(this.input); this.root.add(this.output);
    this.treeModel = new DefaultTreeModel( this.root );
    this.tree = new JTree( this.treeModel );
    this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.tree.setShowsRootHandles( true );
    this.tree.addTreeSelectionListener(new TreeSelectionListener(){
      public final void valueChanged(TreeSelectionEvent e){
        ParametersSetUI.this.processSelection();
      }
    });
    super.add( new JScrollPane(this.tree), BorderLayout.CENTER );
  }
/**
 * <attribute>
 * The control of the parameters's Tree
 * */
private final ControlPanel control = new ControlPanel();
  /**
   * <accessor>
   * To get access to control
   * */
  public final JPanel getControl(){return this.control;}
/**
 * <control>
 * The control panel for the tree
 * */
private final class ControlPanel extends controlPanel{
  public ControlPanel(){super();
    super.getButton("add").setToolTipText("To create a parameter");
    super.getButton("del").setToolTipText("To delete the parameter");
    super.getButton("up").setToolTipText("To move Up the parameter");
    super.getButton("down").setToolTipText("To move Down the parameter");
    super.getButton("edit").setToolTipText("To edit the parameter");
  }
protected final void Add(){ParametersSetUI.this.add();}
protected final void Del(){ParametersSetUI.this.del();}
protected final void Up(){ParametersSetUI.this.up();}
protected final void Down(){ParametersSetUI.this.down();}
protected final void Edit(){ParametersSetUI.this.edit();}
}
/**
 * <attribute>
 * The visual component for edit a Formal parameter
 * */
private final formalEditor formal = new formalEditor();
/**
 * <editor>
 * Class fo edit the formal parameter
 * */
private final class formalEditor extends JPanel{
  /** the name of parameter */
  public JTextField name = new JTextField( 15  );
  /** the type of parameter */
  public JComboBox type = new JComboBox( Symbol.SHOTR_TYPE );
  /** the parameter's default */
  public JTextField defValue = new JTextField();
  /** flag, is assigment in progress */
  private transient boolean isAssign = false;
  /** constructor */
  formalEditor(){super(true);
    super.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    JPanel param = new JPanel( new FlowLayout(FlowLayout.LEFT,2,2) );
    param.add(this.name);param.add(this.type);
    super.add(param); super.add(this.defValue);
    Border border = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(142, 142, 142)),"Formal");
    super.setBorder( border );
    this.type.setEditable(false);
    this.type.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if ( formalEditor.this.isAssign || parameter == null ) return;
        int paramType = type.getSelectedIndex();
        formalEditor.this.parameter.setType( paramType );
        formalEditor.this.defValue.setText("");
        boolean editable = paramType==Symbol.STRING || paramType==Symbol.NUMBER;
        formalEditor.this.defValue.setEditable( editable );
        int aligment = JTextField.LEFT;
        if (paramType == Symbol.NUMBER) aligment = JTextField.RIGHT;
        formalEditor.this.defValue.setHorizontalAlignment(aligment);
      }
    });
    this.name.setDocument(new UI.validatingDocument(){
      /** to validate the string of text field */
      protected final void validate(String str) throws Exception{
        if ( formalEditor.this.isAssign || parameter == null ) return;
        ParametersSetUI.this.validateFormalParameterName(str);
        formalEditor.this.parameter.setName(str);
      }
    });
    this.defValue.setDocument(new UI.validatingDocument(){
      /** to validate the string of text field */
      protected final void validate(String str) throws Exception{
        if ( formalEditor.this.isAssign || parameter == null ) return;
        if ( "".equals(str) ) {parameter.setValue(null);return;}
        Object value = null;
        switch( parameter.getType() ) {
          case Symbol.STRING:
            value = str.toString();
            break;
          case Symbol.NUMBER:
            value = new Double( str );
            break;
        }
        parameter.setValue( value );
      }
    });
  }
  /**<attribute> editable parameter */
  private Parameter.Formal parameter = null;
  /**<mutator> to assign the parameter to the editor */
  public final void assign(Parameter.Formal parameter)
  {
    this.isAssign=true;
    this.parameter=parameter.copy();
    this.name.setText( parameter.getStringName() );
    int paramType = parameter.getType();
    this.type.setSelectedIndex( paramType );
    if (paramType==Symbol.STRING || paramType==Symbol.NUMBER)
    {
      this.defValue.setEditable(true);
      Object value = parameter.getValue();
      String text = value == null ? "":value.toString();
      this.defValue.setText( text );
      if (parameter.getType() == Symbol.NUMBER)
      {
        this.defValue.setHorizontalAlignment(JTextField.RIGHT);
      }else
      {
        this.defValue.setHorizontalAlignment(JTextField.LEFT);
      }
    }else
    {
      this.defValue.setEditable(false);
      this.defValue.setText( "" );
    }
    this.isAssign=false;
  }
  /**<accessor> to get edited parameter */
  public final Parameter.Formal result(){return this.parameter;}
}
/**
 * <accessor>
 * to get access to selected pool of parameters
 * */
private final java.util.List selectedPool(){
  java.util.List pool = null;
  if (this.subtree == this.input) pool = this.params.getInputParameters();
  else
  if (this.subtree == this.output) pool = this.params.getOutputParameters();
  return pool;
}
/**
 * <validator>
 * To validate the formal parameter's name
 * */
private final void validateFormalParameterName(String name) throws Exception {
  java.util.List pool = this.selectedPool();
  if (pool == null) throw new Exception("Invalid selection");
  for(java.util.Iterator i=pool.iterator();i.hasNext();){
    String parName = ((Parameter)i.next()).getStringName();
    if ( parName.equals(name) ) throw new Exception("Name duplication");
  }
}
/**
 * dialog for edit the parameter
 * */
private static uiDialog dialog = null;
private static final Object semaphore=new Object();
/**
 * <producer>
 * To make the dialog
 * */
private final void checkDialog(){
  if (dialog != null) return;
  synchronized( semaphore ) {
      if (dialog == null) {
        Frame frame = JOptionPane.getFrameForComponent(this);
        dialog = new uiDialog(frame);
      }
  }
}
/**
 * To get access to formal parameters
 * @return
 */
private final java.util.List getAvailableFormalParameters(){
  ArrayList parameters = new ArrayList();
  ParametersSet formal = this.getFormalParameters();
  if ( formal == null ) return parameters;
  if ( this.subtree == this.input )
    parameters.addAll( formal.getInputParameters() );
  else
  if ( this.subtree == this.output )
    parameters.addAll( formal.getOutputParameters() );
  return parameters;
}
/**
 * To add parameter
 * @param parameter to add
 */
private final void addParameter(Parameter parameter){
  if ( this.subtree == this.input )
    this.params.addInputParameter(parameter);
  else
  if ( this.subtree == this.output ){
    if (
        parameter instanceof Parameter.Actual                 &&
        ((Parameter.Actual)parameter).getExtenalName().isConst()
        ) return;
    this.params.addOutputParameter(parameter);
  }else return;
  this.assign(this.params,this.paramClass);
  TreePath path = (TreePath)this.paths.get( parameter );
  this.tree.setSelectionPath(path);
}
/**
 * To update the parameter
 * @param Old old parameter
 * @param New new parameter
 */
private final void updateParameter( Parameter Old, Parameter New){
  if ( this.subtree == this.input )
    this.params.updateInputParameter(Old,New);
  else
  if ( this.subtree == this.output )
    this.params.updateOutputParameter(Old,New);
  else return;
  DefaultMutableTreeNode node = this.selected();
  node.setUserObject( New );
  this.treeModel.reload( node );
}
/**
 * <mutator>
 * To add formal parameter
 * */
private final void addFormalParameter(){
  this.checkDialog();
  this.formal.assign( new Parameter.Formal(this.newParamName()) );
  this.formal.name.setEditable( true );
  dialog.setTitle("Adjust new parameter");
  dialog.setEditor( this.formal );
  dialog.setVisible( true );
  if ( dialog.isAccept() ) this.addParameter( this.formal.result() );
}
/**
 * <mutator>
 * To edit formal parameter
 * */
private final void editFormalParameter(){
  Parameter.Formal param = (Parameter.Formal)this.selectedObject();
  this.checkDialog();
  this.formal.assign( param.copy() );
  this.formal.name.setEditable( false );
  dialog.setTitle("Adjust the parameter");
  dialog.setEditor( this.formal );
  dialog.setVisible( true );
  if ( dialog.isAccept() ) this.updateParameter( param, this.formal.result() );
}
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, class for edit the actual parameter</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
private final class actualEditor extends JPanel{
  /**
   * <editor>
   * <p>Title: Visual CTI Java Telephony Server</p>
   * <p>Description: VisualCTI WorkFlow, to edit the value of parameter</p>
   * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
   * <p>Company: Prominic Ukraine Co</p>
   * @author Sopilnyak Oleg
   * @version 1.0
   */
  private final class valueEditor extends SymbolEditor{
    public valueEditor(){super();
      super.name.getDocument().addDocumentListener( new DocListener(){
        protected final void update() {Symbol symbol=getSymbol();
          if ( isAssign || symbol==null || !symbol.isConst() ) return;
          symbol.setName( name.getText() );
        }
      });
    }
    protected final java.util.List availableGroups(){return getAvailabledGroups();}
    protected final SymbolChooser getSymbolChooser(){return getDialog();}
    protected final Symbol getSymbol(){return owner == null ? null:owner.getExtenalName();}
    protected final void setSymbol(Symbol symbol){
      if (owner != null) owner.setExtrenalName(symbol);
    }
    protected final Symbol getConst(){return Symbol.newConst("");}
  }
  /**
   * <listener>
   * The listener of document's changes
   * */
  private abstract class DocListener implements DocumentListener {
    public final void insertUpdate(DocumentEvent e) {this.update();}
    public final void removeUpdate(DocumentEvent e) {this.update();}
    public final void changedUpdate(DocumentEvent e){}
    /** to check and process changes */
    protected abstract void update();
  }
  private final SymbolChooser getDialog(){
    int typeID = this.owner.getNameNative().getTypeID();
    SymbolChooser dialog = SymbolChooser.getInstance(this);
    ArrayList list = new ArrayList();
    java.util.List all = getAvailabledSymbols();
    if ( all != null) {
      for(Iterator i=all.iterator();i.hasNext();) {
        Symbol symbol=(Symbol)i.next();
        if (symbol!=null && symbol.getTypeID()==typeID) list.add(symbol);
      }
    }
    dialog.setSymbols( list );
    return dialog;
  }
  /**
   * the editable parameter
   */
  private transient Parameter.Actual owner=null;
  private transient boolean isAssign = false;
  /**
   * To assign the parameter with editor
   * @param owner the editable Parameter
   */
  void assign(Parameter.Actual owner){
    this.isAssign = true;
    this.parameters.setSelectedItem( this.owner=owner );
    this.value.reload();
    this.isAssign = false;
  }
  /**
   * To assign available formal parameters set
   * @param set the set of parameters
   */
  void setFormalParameters(java.util.List set){
    this.parameters.removeAllElements();
    for(Iterator i=set.iterator();i.hasNext();) this.parameters.addElement(i.next());
  }
  /**
   * The model of combobox
   */
  private final DefaultComboBoxModel parameters = new DefaultComboBoxModel();
  /**
   * The list of formal parameters
   * */
  private final JComboBox paramsList = new JComboBox( parameters );
  /**
   * editor for value
   */
  private final valueEditor value = new valueEditor();
  /**
   * <constructor>
   * To make the editor
   */
  actualEditor(){super(true);
    Border border = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(142, 142, 142)),"Actual");
    super.setBorder( border );
    super.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    JPanel parameter = new JPanel(new BorderLayout(5,0));
    super.add(parameter); super.add(this.value);
    this.value.title.setText("Value");
    this.value.name.setColumns(20);
    this.paramsList.addActionListener(new ActionListener(){
      public final void actionPerformed(ActionEvent e){
        if ( isAssign ) return;
        Parameter.Formal param = (Parameter.Formal)paramsList.getSelectedItem();
        if ( param != null ) assign( new Parameter.Actual(param) );
      }
    });
    parameter.add(new JLabel("Parameter's name"),BorderLayout.WEST);
    parameter.add( this.paramsList, BorderLayout.CENTER );
  }
  /**<accessor> to get edited parameter */
  public final Parameter.Actual result(){return this.owner;}
}
/**
 * <editor>
 * The editor for edit actual parameter
 */
private final actualEditor actual = new actualEditor();
/**
 * <accessor>
 * To get access to formal parameters of this Actual ParametersSet
 * This method will overrided in SubroutineUI
 * */
protected ParametersSet getFormalParameters(){return null;}
protected java.util.List getAvailabledGroups(){return null;}
protected java.util.List getAvailabledSymbols(){return null;}
/**
 * <mutator>
 * To add actual parameter
 * */
private final void addActualParameter(){
  this.checkDialog();
  java.util.List formalSet = this.getValidFormalParameters();
  if ( formalSet.size() == 0 ){
    JOptionPane.showMessageDialog
                  (
                  dialog,
                  "No availabled formal parameters",
                  "Adjust new parameter",
                  JOptionPane.INFORMATION_MESSAGE
                  );
    return;
  }
  this.actual.setFormalParameters( formalSet );
  this.actual.assign( new Parameter.Actual((Parameter.Formal)formalSet.get(0)) );
  this.actual.paramsList.setEnabled(true);
  dialog.setTitle("Adjust new parameter");
  dialog.setEditor( this.actual );
  dialog.setVisible( true );
  if ( dialog.isAccept() ) this.addParameter( this.actual.result() );
}
/**
 * <accessor>
 * To get access to actual parameters set
 * @return
 */
private final java.util.List getActualParameters(){
  ArrayList actual = new ArrayList();
  if ( this.subtree == this.input )
    actual.addAll(this.params.getInputParameters() );
  else
  if ( this.subtree == this.output )
    actual.addAll(this.params.getOutputParameters() );
  else actual = null;
  return actual;
}
/**
 * <accessor>
 * To get access to availabled formal parameters
 * @return the list
 */
private final java.util.List getValidFormalParameters(){
  ArrayList result = new ArrayList();
  java.util.List actual = this.getActualParameters();
  java.util.List formal = this.getAvailableFormalParameters();
  // to iterated the formal parameters list
  for(Iterator i=formal.iterator();i.hasNext();){
    Parameter par = (Parameter)i.next();
    if ( actual == null ) {
      result.add( par );// no actual parameters
    }else
    if ( actual.indexOf(par) == -1 ){
      result.add( par );// formal parameters not exist in actual's list
    }
  }
  return result;
}
/**
 * <mutator>
 * To edit actual parameter
 * */
private final void editActualParameter(){
  this.checkDialog();
  java.util.List formalSet = this.getAvailableFormalParameters();
  if ( formalSet.size() == 0 ){
    JOptionPane.showMessageDialog
                  (
                  dialog,
                  "No availabled formal parameters",
                  "Adjust the parameter",
                  JOptionPane.INFORMATION_MESSAGE
                  );
    return;
  }
  this.actual.setFormalParameters( formalSet );
  Parameter.Actual old = (Parameter.Actual)this.selectedObject();
  this.actual.assign( old );
  this.actual.paramsList.setEnabled(false);
  dialog.setTitle("Adjust the parameter ["+old.getStringName()+"]");
  dialog.setEditor( this.actual );
  dialog.setVisible( true );
  if ( dialog.isAccept() ) this.updateParameter( old, this.actual.result() );
}
/**
 * <mutator>
 * To add the parameter
 * */
private final void add(){
  if ( this.paramClass == Parameter.Formal.class ) {
    this.addFormalParameter();
  } else
  if ( this.paramClass == Parameter.Actual.class ) {
    this.addActualParameter();
  }
}
/**
 * <mutator>
 * To delete parameter
 * */
private final void del(){
  Object value = this.selectedObject();
  if ( value instanceof Parameter ) {
    Parameter param = (Parameter)value;
    DefaultMutableTreeNode subtreeRoot=null;
    if ( this.subtree == this.input ) {
      param = this.params.deleteInputParameter(param);
      subtreeRoot = this.input;
    }else
    if ( this.subtree == this.output ) {
      param = this.params.deleteOutputParameter(param);
      subtreeRoot = this.output;
    }else return;
    this.assign(this.params,this.paramClass);
    TreePath toSelect = null;
    if ( param == null ){
      toSelect = new TreePath(subtreeRoot.getPath());
    }else {
      toSelect = (TreePath)this.paths.get( param );
    }
    this.tree.setSelectionPath( toSelect );
  }
}
/**
 * <mutator>
 * To move up the parameter
 * */
private final void up(){
  Object value = this.selectedObject();
  if ( value instanceof Parameter ) {
    Parameter param = (Parameter)value;
    boolean moved = false;
    if ( this.subtree == this.input ) {
      moved = this.params.moveUpInputParameter(param);
    }else
    if ( this.subtree == this.output ) {
      moved = this.params.moveUpOutputParameter(param);
    }else return;
    if ( moved ) {
      this.assign(this.params,this.paramClass);
      this.tree.setSelectionPath( (TreePath)this.paths.get(param) );
    }
  }
}
/**
 * <mutator>
 * To move down the parameter
 * */
private final void down(){
  Object value = this.selectedObject();
  if ( value instanceof Parameter ) {
    Parameter param = (Parameter)value;
    boolean moved = false;
    if ( this.subtree == this.input ) {
      moved = this.params.moveDownInputParameter(param);
    }else
    if ( this.subtree == this.output ) {
      moved = this.params.moveDownOutputParameter(param);
    }else return;
    this.assign(this.params,this.paramClass);
    this.tree.setSelectionPath( (TreePath)this.paths.get(param) );
  }
}
/**
 * <mutator>
 * To edit selected parameter
 * */
private final void edit(){
  if ( this.paramClass == Parameter.Formal.class ) {
    this.editFormalParameter();
  } else
  if ( this.paramClass == Parameter.Actual.class ) {
    this.editActualParameter();
  }
  this.treeModel.reload( this.selected() );
}
/**
 * <attribute>
 * The parameters set for editing
 * */
private ParametersSet params = null;
/**
 * <attribute>
 * The class of content's objects
 * */
private Class paramClass = null;
/**
 * <pool>
 * The node's values and paths to it
 * */
private final java.util.HashMap paths = new java.util.HashMap();
  /**
   * <assign>
   * To assign the ParametersSet with this UI
   * */
  public final void assign(ParametersSet params,Class paramClass)
  {
    this.params=params; this.paramClass=paramClass; this.paths.clear();
    this.fill( this.input,params.getInputParameters() );
    this.fill( this.output,params.getOutputParameters() );
    this.treeModel.reload( this.root );
    this.tree.expandPath( new TreePath(this.input.getPath()) );
    this.tree.expandPath( new TreePath(this.output.getPath()) );
    this.control.disableAll();
  }
  /**
   * <fill>
   * To fill the subtree
   * */
  private final void fill(DefaultMutableTreeNode root,java.util.List params){
    root.removeAllChildren();
    for(java.util.Iterator i=params.iterator();i.hasNext();){
      Parameter param = (Parameter)i.next();
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(param);
      root.add( node ); this.paths.put(param, new TreePath(node.getPath()) );
    }
  }
  /**
   * <producer>
   * To make unique name for this parameters list
   * */
  private String newParamName()
  {
    java.util.List container = this.selectedPool();
    if (container == null) return "?????";
    // try to find unique name of new parameter
    String name = "param";int index=1;boolean finded = false;
    while( true ) {
      finded = true;
      for(Iterator i=container.iterator();i.hasNext();){
        String parName = ((Parameter)i.next()).getStringName();
        if ( parName.equals(name) ) {finded = false;break;}
      }
      if ( finded ) return name;
      else {name = "param"+index;index++;}
    }
  }
  /**
   * to delete selected pair
   * */
  private void delPair(){
    DefaultMutableTreeNode node = this.selected();
    DefaultMutableTreeNode parent = this.pool( node );
    if (parent == null) return;
    Object o = node.getUserObject();
    /*
    if (o instanceof ParametersSet.Pair) {
      java.util.List container =
        parent == this.input ? this.set.input():this.set.output();
      if ( !container.remove( o ) ) return;
      //JTree tree = this.parameters;
      this.treeModel.removeNodeFromParent(node);
      TreePath toSelection = new TreePath( parent.getPath() );
      this.parameters.setSelectionPath( toSelection );
    }
    */
  }
  private final DefaultMutableTreeNode pool(DefaultMutableTreeNode node)
  {
    if (node == this.input || node == this.output || node == null) return node;
    return this.pool((DefaultMutableTreeNode)node.getParent());
  }
/**
 * <attribute>
 * The owner of current subtree
 * */
private DefaultMutableTreeNode subtree = null;
  /**
   * to process selection
   * */
  private final void processSelection()
  {
      DefaultMutableTreeNode node=this.selected();
      this.subtree = null;
      if ( node == null) return;
      TreePath path = new TreePath( node.getPath() );
      Object value = node.getUserObject();
      if ( node == this.root) {
        this.control.disableAll();
      }else
      if( node == this.input || node == this.output) {
        if ( tree.isCollapsed(path) ) tree.expandPath(path);
        this.subtree = node;
        this.control.addOnly();
      }else
      if (value instanceof Parameter) {
        this.subtree = (DefaultMutableTreeNode)node.getParent();
        this.control.allFeatures();
      }
  }
  /**
   * <accessor>
   * To get selected node
   * */
  private final DefaultMutableTreeNode selected(){
    return (DefaultMutableTreeNode)this.tree.getLastSelectedPathComponent();
  }
  /**
   * <accessor>
   * To get access to selected Object
   * */
  private final Object selectedObject(){
    TreePath path = this.tree.getSelectionPath();
    if (path == null) return null;
    Object node = path.getLastPathComponent();
    if ( node instanceof DefaultMutableTreeNode ){
      return ((DefaultMutableTreeNode)node).getUserObject();
    }
    return null;
  }
}
