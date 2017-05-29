package com.proyecto;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.proyecto.security.SecurityUtils;
import com.vaadin.data.Binder;
import com.vaadin.data.converter.StringToLongConverter;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
@SpringComponent
@UIScope
public class OrderPEditor extends VerticalLayout{
	
	private final OrderPRepository repository;
	
	private final ProductRepository repoP;
	
	private final ZonaRepository repoZona;
	
	private final MenuRepository repoM;
	
	private final CustomerRepository repoC;
	
	private final OrderLineMenuRepository repoOLM;
	
	private final OrderLineProductRepository repoOLP;
	
	private final NativeSelect<String> zonasSelect;
	
	protected List<OrderLineProduct> orderLPlist = new ArrayList<>();
	
	protected List<OrderLineMenu> orderLMlist = new ArrayList<>();

	private OrderP orderp;
	private OrderLineProduct orderLP;
	/*private Menu menu;
	private ProductMenu productMenu;
	private Customer customer;*/
	
	final Grid<Customer> gridCustomer = new Grid<Customer>(Customer.class);
	final Grid<GridTicket> gridTicket = new Grid<GridTicket>(GridTicket.class);
	
	private List<GridTicket> gtList = new ArrayList<GridTicket>();
	
	private TextField filter = new TextField();
	VerticalLayout gridCliente = new VerticalLayout(filter, gridCustomer);
	
	CheckBox takeAway = new CheckBox("Para llevar");
	CheckBox state = new CheckBox("Finalizado");
	TextField numMesa = new TextField("Numero de mesa");
	
	Button save = new Button("Guardar");
	Button cancel = new Button("Cancelar");
	Button delete = new Button("Eliminar");
	CssLayout actions = new CssLayout(save, cancel, delete);
	
	TabSheet alimentos = new TabSheet();

	GridLayout comidasLayout = new GridLayout();
	GridLayout bebidasLayout = new GridLayout();
	GridLayout postresLayout = new GridLayout();
	GridLayout menusLayout = new GridLayout();
	
	
	Button addMenu = new Button("Añadir");
	Button addProduct = new Button("Añadir");
	
	private File file;
	
	Binder<OrderP> binder = new Binder<>(OrderP.class);
	
	public OrderPEditor(OrderPRepository repository, ProductRepository repoProduct
						, ZonaRepository repoZona, MenuRepository repoM, CustomerRepository repoC, 
						OrderLineMenuRepository repoOLM, OrderLineProductRepository repoOLP){
		this.repoP = repoProduct;
		this.repository = repository;
		this.repoZona = repoZona;
		this.repoC = repoC;
		this.repoM = repoM;
		this.repoOLM = repoOLM;
		this.repoOLP = repoOLP;
		
		menusLayout.setColumns(5);
	    comidasLayout.setColumns(5);
		bebidasLayout.setColumns(5);
		postresLayout.setColumns(5);
			
		List<Customer> listCustomer = repoC.findAll();
		gridCustomer.setColumns();
		gridCustomer.addColumn(customer -> { return customer.getDireccion(); }).setCaption("Cliente");
		gridCustomer.addColumn(customer -> { return customer.getTelefono(); }).setCaption("Telefono");
		gridCustomer.setItems(listCustomer);
		gridCliente.setVisible(false);
		
		filter.setPlaceholder("Filtrar por telefono");

		filter.setValueChangeMode(ValueChangeMode.LAZY);
		filter.addValueChangeListener(e -> listPhones(e.getValue()));
		
		gridTicket.setColumns();
		gridTicket.addColumn(ticket -> { return ticket.getNombre();}).setCaption("Nombre");
		gridTicket.addColumn(ticket -> { return ticket.getCantidad(); }).setCaption("Cantidad");
		gridTicket.addColumn(ticket -> { return ticket.getPrecio(); }).setCaption("Precio");
		
		//Lista de zonas existentes
		List<Zona> zonasList = repoZona.findAll();
		ArrayList<String> zonasName = new ArrayList<>();
		for(Zona z: zonasList)
			zonasName.add(z.getName());
		zonasSelect = new NativeSelect<>("Selecciona zona", zonasName);
		
		//Lista de comidas existentes
		Collection<Product> comidas = repoP.findByFamily("comidas");
		for(Product p: comidas){
			//System.out.println(p.getProductImage());
			file = new File(p.getProductImage());
			Image image = new Image(p.getName());
			image.setWidth(100, Unit.PIXELS);
			image.setHeight(100, Unit.PIXELS);
			image.setSource(new FileResource(file));
			image.addClickListener(e -> insertarProducto(image.getCaption(),orderp));
			comidasLayout.addComponent(image);
		}
		alimentos.addTab(comidasLayout, "Comidas");
		
		//Lista de bebidas existentes
		Collection<Product> bebidas = repoP.findByFamily("bebidas");
		for(Product p: bebidas){
			//System.out.println(p.getProductImage());
			file = new File(p.getProductImage());
			Image image = new Image(p.getName());
			image.setWidth(100, Unit.PIXELS);
			image.setHeight(100, Unit.PIXELS);
			image.setSource(new FileResource(file));
			image.addClickListener(e -> insertarProducto(image.getCaption(),orderp));
			bebidasLayout.addComponent(image);
		}
		alimentos.addTab(bebidasLayout, "Bebidas");
		
		//Lista de postres existentes
		Collection<Product> postres = repoP.findByFamily("postres");
		for(Product p: postres){
			//System.out.println(p.getProductImage());
			file = new File(p.getProductImage());
			Image image = new Image(p.getName());
			image.setWidth(100, Unit.PIXELS);
			image.setHeight(100, Unit.PIXELS);
			image.setSource(new FileResource(file));
			image.addClickListener(e -> insertarProducto(image.getCaption(), orderp));
			postresLayout.addComponent(image);
		}
		alimentos.addTab(postresLayout, "Postres");
		
		//Lista de menus existentes
		Collection<Menu> menus = repoM.findAll();
		for(Menu m: menus){
			file = new File(m.getMenuImage());
			Image image = new Image(m.getName());
			image.setWidth(100, Unit.PIXELS);
			image.setHeight(100, Unit.PIXELS);
			image.setSource(new FileResource(file));
			image.addClickListener(e -> insertarMenu(image.getCaption(), orderp));
			menusLayout.addComponent(image);
		}
		alimentos.addTab(menusLayout, "Menus");
		addComponents(state, takeAway, gridCliente, numMesa, zonasSelect, gridTicket, alimentos, actions);
		
		binder.forField(numMesa)
		  .withNullRepresentation("")
		  .withConverter(
		    new StringToLongConverter("Por favor introduce un número"))
		  .bind("numMesa");
		
		binder.bindInstanceFields(this);
		
		//gridProdAct.asSingleSelect().addValueChangeListener(e -> { editProdAct(e.getValue()); });
		
		setSpacing(true);
		actions.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		save.setStyleName(ValoTheme.BUTTON_PRIMARY);
		save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		
		takeAway.addValueChangeListener(e -> {
			if(e.getValue() == false){
				gridCliente.setVisible(false);
				zonasSelect.setVisible(true);
				numMesa.setVisible(true);
			}
			else{
				gridCliente.setVisible(true);
				zonasSelect.setVisible(false);
				numMesa.setVisible(false);
			}
		});
		
		listPhones(null);
		
		gridCustomer.asSingleSelect().addValueChangeListener(e -> { orderp.setCustomer(e.getValue()); });
		
		save.addClickListener(e -> guardarPedido(orderp));
		delete.addClickListener(e -> eliminarPedido(orderp));
		//cancel.addClickListener(e -> editOrderP(orderp));
		setVisible(false);
	}
	
	
	public final void insertarMenu(String name, OrderP orderp){
		List<Menu> menuList = repoM.findByNameStartsWithIgnoreCase(name);
		gtList = orderp.getGridTicketList();
		
		boolean existe = false;
		int pos = 0;
		while(!existe && pos<gtList.size()){
			if(name == gtList.get(pos).getNombre())
				existe = true;
			pos++;
		}
		pos--;
		
		if(existe){
			gtList.get(pos).setCantidad(gtList.get(pos).getCantidad()+1);
			gtList.get(pos).setPrecio(Double.parseDouble(menuList.get(0).getPrice()) * gtList.get(pos).getCantidad());
			orderLMlist.add(new OrderLineMenu(1, Double.parseDouble(menuList.get(0).getPrice()), 
					orderp, menuList.get(0)));
			
    	}else{
			gtList.add(new GridTicket(name, 1L, Double.parseDouble(menuList.get(0).getPrice()), true));
			orderLMlist.add(new OrderLineMenu(1, Double.parseDouble(menuList.get(0).getPrice()), 
					orderp, menuList.get(0)));
    	}
		
		gridTicket.setItems(gtList);
		orderp.setOrderLineMenuList(orderLMlist);
	}
	
	public final void insertarProducto(String name, OrderP orderp){
		List<Product> productList = repoP.findByNameStartsWithIgnoreCase(name);
		gtList = orderp.getGridTicketList();
		System.out.println("Tamaño de gtList: "+gtList.size());
		System.out.println(productList.get(0));
		boolean existe = false;
		int pos = 0;
		while(!existe && pos<gtList.size()){
			if(name.equals(gtList.get(pos).getNombre()))
				existe = true;
			pos++;
		}
		pos--;
		
		if(existe){
			gtList.get(pos).setCantidad(gtList.get(pos).getCantidad()+1);
			gtList.get(pos).setPrecio(Double.parseDouble(productList.get(0).getPrice()) * gtList.get(pos).getCantidad());
			orderLPlist.get(pos).setCantidad(orderLPlist.get(pos).getCantidad()+1);
			orderLPlist.get(pos).setPrecio(orderLPlist.get(pos).getPrecio()*orderLPlist.get(pos).getCantidad());
    	}else{
			gtList.add(new GridTicket(name, 1L, Double.parseDouble(productList.get(0).getPrice()), true));
			System.out.println("Orderp:"+orderp);
			System.out.println("Precio: "+Double.parseDouble(productList.get(0).getPrice()));
			System.out.println("producto: "+productList.get(0));
			OrderLineProduct o = new OrderLineProduct(1, Double.parseDouble(productList.get(0).getPrice()), orderp, productList.get(0));
			System.out.println("Objeto: "+o);
			orderLPlist.add(o);
    	}
		
		gridTicket.setItems(gtList);
		orderp.setOrderLineProductList(orderLPlist);
	}
	
	public void guardarPedido(OrderP p){
		p.setUser(SecurityUtils.getUserLogin());
		costeTotal(gtList);
		//primero controlamos si el pedido es para llevar o no
		if(takeAway.getValue()){
			p.setState(true);
			System.out.println("Accediendo a guardado");
			p.setOrderLineProductList(orderLPlist);
			repository.save(p);
			for(OrderLineProduct item: orderLPlist)
				repoOLP.save(item);
			for(OrderLineMenu item: orderLMlist)
				repoOLM.save(item);
		}
		else{
			System.out.println("Valor a falso");
			String zonaName = zonasSelect.getValue();
			List<Zona> z = repoZona.findByNameStartsWithIgnoreCase(zonaName);
			orderp.setZona(z.get(0));
			orderp.setNumMesa(Long.parseLong(numMesa.getValue()));
			p.setOrderLineProductList(orderLPlist);
			repository.save(p);
			for(OrderLineProduct item: orderLPlist)
				repoOLP.save(item);
			for(OrderLineMenu item: orderLMlist)
				repoOLM.save(item);
			
		}
	}
	
	public void eliminarPedido(OrderP orderp){
		repository.delete(orderp);
	}
	public interface ChangeHandler {
		void onChange();
	}

	public final void editOrderP(OrderP or) {
		if (or == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = or.getId() != null;
		if (persisted) {
			System.out.println("Existe en la bd");
			//gridProdAct.setItems();
			orderp = repository.findOne(or.getId());
			//System.out.println("Id de orderp: "+orderp.getId());
			//List<OrderLineMenu> l = repoOLM.findByIdMenu(orderp.getId());
			orderp.setOrderLineMenuList(repoOLM.findByIdMenu(orderp.getId()));
			orderp.setOrderLineProductList(repoOLP.findByIdProduct(orderp.getId()));
			System.out.println("tam productos: "+orderp.getOrderLineProductList().size());
			System.out.println("tam menus: "+orderp.getOrderLineMenuList().size());
			List<GridTicket> items = new ArrayList<>();
			for(OrderLineProduct i: orderp.getOrderLineProductList())
				items.add(new GridTicket(i.getProductObj().getName(), new Long(i.getCantidad()), i.getPrecio(), true));
			for(OrderLineMenu i: orderp.getOrderLineMenuList())
				items.add(new GridTicket(i.getMenuObj().getName(), new Long(i.getCantidad()), i.getPrecio(), true));
			
			//System.out.println("El ticket tiene: "+items.size());
			gridTicket.setItems(items);
			orderp.setGridTicketList(items);
		}
		else {
			System.out.println("Nuevo");
			gridTicket.setItems();
			orderp = or;
		}
		cancel.setVisible(persisted);

		binder.setBean(orderp);

		setVisible(true);
		save.focus();
	}

	public void setChangeHandler(ChangeHandler h) {
		save.addClickListener(e -> h.onChange());
		delete.addClickListener(e -> h.onChange());
	}
	
	 void listPhones(String filterText) {
		if (StringUtils.isEmpty(filterText)) {
			gridCustomer.setItems((Collection<Customer>) repoC.findAll());
		}
		else {
			gridCustomer.setItems(repoC.findBytelefonoStartsWithIgnoreCase(filterText));
		}
	}
	 
	 public void costeTotal(List<GridTicket> gtList){
		 double precioTotal = 0.0;
		 for(GridTicket item: gtList)
			 precioTotal += item.getPrecio();
		 
		 orderp.setCoste(precioTotal);
	 }
}