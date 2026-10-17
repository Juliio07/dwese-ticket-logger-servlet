package org.iesalixar.daw2.julio.servlets;

import org.iesalixar.daw2.julio.dao.*;
import org.iesalixar.daw2.julio.entity.Province;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.iesalixar.daw2.julio.entity.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/**
 * Servlet que gestiona el CRUD de provincias a través de la URL "/provinces".
 * Controla las validaciones (campos vacíos y código repetido) y delega el
 * acceso a datos en {@link ProvinceDAO} y {@link RegionDAO}.
 */

@WebServlet("/provinces")
public class ProvinceServlet extends HttpServlet{
    // Logger para registrar entradas, salidas, validaciones y errores
    private static final Logger logger = LoggerFactory.getLogger(ProvinceServlet.class);

    private RegionDAO regionDAO;
    private ProvinceDAO provinceDAO;
    /**
     * Inicializa el servlet creando los DAO necesarios.
     *
     * @throws ServletException si no se pueden crear los DAO.
     */
    @Override
    public void init() throws ServletException{
        logger.info("Entrando en el método init de ProvinceServlet");
        try{
            regionDAO=new RegionDAOImpl();
            provinceDAO=new ProvinceDAOImpl();
        }catch (Exception e){
            throw new ServletException("Error al iniciar ProvinceDAO", e);
        }
        logger.info("Saliendo del método init de ProvinceServlet");
    }
    /**
     * Maneja las solicitudes GET al servlet. Según el parámetro "action", decide
     qué método invocar.
     * @param request Solicitud HTTP.
     * @param response Respuesta HTTP.
     * @throws ServletException en caso de errores en el servlet.
     * @throws IOException en caso de errores de E/S.
     */

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        logger.info("Entrando en el método doGet de ProvinceServlet");
        String action=request.getParameter("action");
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try{
            if(action == null){
                action = "list";
            }
            logger.info("Acción GET recibida: {}", action);
            switch (action) {
                case "new":
                    showNewForm(request, response); // Mostrar formulario para nueva región
                    break;
                case "edit":
                    showEditForm(request, response); // Mostrar formulario para editar región
                    break;
                default:
                    listProvince(request, response); // Listar todas las regiones
                    break;
            }
        } catch (SQLException ex) {
            logger.error("Error de base de datos en doGet: {}", ex.getMessage(), ex);
            throw new ServletException(ex);
        }
        logger.info("Saliendo del método doGet de ProvinceServlet");
    }

    /**
     * Maneja las solicitudes POST al servlet. Según el parámetro "action",
     decide qué método invocar.
     * @param request Solicitud HTTP.
     * @param response Respuesta HTTP.
     * @throws ServletException en caso de errores en el servlet.
     * @throws IOException en caso de errores de E/S.
     */

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Entrando en el método doPost de ProvinceServlet");
        String action = request.getParameter("action");
        logger.info("Acción POST recibida: {}", action);
        try {
            switch (action) {
                case "insert":
                    insertProvince(request, response); // Insertar nueva región
                    break;
                case "update":
                    updateProvince(request, response); // Actualizar región existente
                    break;
                case "delete":
                    deleteProvince(request, response); // Eliminar región
                    break;
                default:
                    listProvince(request, response); // Listar todas las regiones
                    break;
            }
        } catch (SQLException ex) {
            logger.error("Error de base de datos en doPost: {}", ex.getMessage(), ex);
            throw new ServletException(ex);
        }
        logger.info("Saliendo del método doPost de ProvinceServlet");
    }

    private void listProvince(HttpServletRequest request, HttpServletResponse response) throws SQLException,IOException,ServletException{
        logger.info("Entrando en el método listProvince");
        List<Province> listProvinces = provinceDAO.listAllProvinces();
        Map<Integer, String> regionNames = new HashMap<>();
        for (Region r : regionDAO.listAllRegions()) {
            regionNames.put(r.getId(), r.getName());
        }
        logger.info("Se han cargado {} provincias", listProvinces.size());
        request.setAttribute("listProvinces", listProvinces);
        request.setAttribute("regionNames", regionNames);
        request.getRequestDispatcher("province.jsp").forward(request, response);
        logger.info("Saliendo del método listProvince");
    }
    /**
     * Muestra el formulario para crear una nueva región.
     * @param request Solicitud HTTP.
     * @param response Respuesta HTTP.
     * @throws ServletException en caso de error en el servlet.
     * @throws IOException en caso de error de E/S.
     */
    private void showNewForm(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException, SQLException {
        logger.info("Entrando en el método showNewForm");
        request.setAttribute("listRegions", regionDAO.listAllRegions());
        request.getRequestDispatcher("province-form.jsp").forward(request, response);
        logger.info("Saliendo del método showNewForm");
    }
    /**
     * Muestra el formulario para editar una región existente.
     * @param request Solicitud HTTP.
     * @param response Respuesta HTTP.
     * @throws SQLException en caso de error en la consulta SQL.
     * @throws ServletException en caso de error en el servlet.
     * @throws IOException en caso de error de E/S.
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)throws SQLException,IOException,ServletException{
        logger.info("Entrando en el método showEditForm");
        int id=Integer.parseInt(request.getParameter("id"));
        Province existingProvince=provinceDAO.getProvinceById(id);
        request.setAttribute("listRegions", regionDAO.listAllRegions());
        request.setAttribute("province",existingProvince);
        request.getRequestDispatcher("province-form.jsp").forward(request, response);
        logger.info("Saliendo del método showEditForm");
    }
    /**
     * Inserta una nueva provincia en la base de datos después de realizar
     validaciones.
     * Verifica que el código de la provincia sea único (ignorando mayúsculas) y
     que los campos
     * de código y nombre no estén vacíos.
     *
     * @param request la solicitud HTTP con los datos del formulario.
     * @param response la respuesta HTTP para redirigir o mostrar errores.
     * @throws SQLException si ocurre un error en la base de datos.
     * @throws IOException si ocurre un error de entrada/salida.
     * @throws ServletException si ocurre un error en el procesamiento del
    servlet.
     */
    public void insertProvince(HttpServletRequest request, HttpServletResponse response)throws SQLException,ServletException,IOException{
        logger.info("Entrando en el método insertProvince");
        String code=request.getParameter("code").trim().toUpperCase();
        String name=request.getParameter("name").trim();
        int id_region=Integer.parseInt(request.getParameter("id_region"));

        if(code.isEmpty() || name.isEmpty()){
            logger.warn("Validación fallida al insertar: hay campos vacíos");
            request.setAttribute("errorMessage", "El código y el nombre no pueden estar vacíos.");
            request.getRequestDispatcher("province-form.jsp").forward(request, response);
            return;
        }
        if (provinceDAO.existsProvinceByCode(code)) {
            logger.warn("Validación fallida al insertar: el código {} ya existe", code);
            request.setAttribute("errorMessage", "El código de la provincia ya existe.");
                    request.getRequestDispatcher("province-form.jsp").forward(request, response);
            return;
        }

        Province newProvince = new Province(code, name, id_region);
        try{
            provinceDAO.insertProvince(newProvince);
        }catch(SQLException e){
            if(e.getSQLState().equals("23505")){//  Código SQL para unique constraint violation
                request.setAttribute("errorMessage", "El código de la provincia debe ser único");
                request.getRequestDispatcher("province-form.jsp").forward(request, response);
            }else{
                throw e;
            }
        }
        response.sendRedirect("provinces");
        logger.info("Saliendo del método insertProvince");
    }
    /**
     * Actualiza una provincia existente en la base de datos después de realizar
     validaciones.
     * Verifica que el código de la provincia sea único para otras provincias
     (ignorando mayúsculas)
     * y que los campos de código y nombre no estén vacíos.
     *
     * @param request la solicitud HTTP con los datos del formulario.
     * @param response la respuesta HTTP para redirigir o mostrar errores.
     * @throws SQLException si ocurre un error en la base de datos.
     * @throws IOException si ocurre un error de entrada/salida.
     * @throws ServletException si ocurre un error en el procesamiento del
    servlet.
     */

    private void updateProvince(HttpServletRequest request, HttpServletResponse response)throws SQLException, IOException, ServletException{
        logger.info("Entrando en el método updateProvince");
        int id=Integer.parseInt(request.getParameter("id"));
        String code=request.getParameter("code").trim().toUpperCase();
        String name=request.getParameter("name").trim();
        int id_region=Integer.parseInt(request.getParameter("id_region"));
        if(code.isEmpty() || name.isEmpty()){
            logger.warn("Validación fallida al actualizar la provincia {}: hay campos vacíos", id);
            request.setAttribute("errorMessage", "El código y el nombre no pueden estar vacíos");
            request.getRequestDispatcher("province-form.jsp").forward(request, response);
            return;
        }

        //Ahora validamos si el codigo ya existe para otra provincia
        if(provinceDAO.existsProvinceByCodeAndNotId(code, id)){
            logger.warn("Validación fallida al actualizar: el código {} ya existe en otra provincia", code);
            request.setAttribute("errorMessage", "El código de la provincia ya existe para otra provincia");
            request.getRequestDispatcher("province-form.jsp").forward(request, response);
            return;
        }
        Province updatedProvince=new Province(id, code, name, id_region);
        try{
            provinceDAO.updateProvince(updatedProvince);
        }catch(SQLException e){
            if(e.getSQLState().equals("23505")){
                request.setAttribute("errorMessage", "El codigo de la provincia debe ser unico");
                request.getRequestDispatcher("province-form.jsp").forward(request, response);
            }else{
                throw e;
            }
        }
        response.sendRedirect("provinces");
    }
    /**
     * Elimina una provincia de la base de datos según su ID.
     * @param request Solicitud HTTP.
     * @param response Respuesta HTTP.
     * @throws SQLException en caso de error en la consulta SQL.
     * @throws IOException en caso de error de E/S.
     */

    private void deleteProvince(HttpServletRequest request, HttpServletResponse response)throws SQLException,IOException{
        logger.info("Entrando en el método deleteProvince");
        int id=Integer.parseInt(request.getParameter("id"));
        provinceDAO.deleteProvince(id);//Elimina la provincia usando DAO
        logger.info("Provincia {} eliminada correctamente", id);
        response.sendRedirect("provinces");//Redirige al listado de provincias
        logger.info("Saliendo del método deleteProvince");
    }
}
