/**
 * This is the customized function that initialize map from openweathermap.org,
 * this is basically a hack, check their website for newer parameters if this
 * script is invalid.
 *
 * @param mapId
 *            the id of the map element in html.
 */
function initMap(mapId) {
    var layer_name = "precipitation";
    var lat = 0;
    var lon = 0;
    var zoom = 2;
    var opacity = 0.6;

    var args = OpenLayers.Util.getParameters();
    if (args.l)	{
        layer_name = args.l;
    }
    if (args.lat) {
        lat = args.lat;
    }
    if (args.lon) {
        lon = args.lon;
    }
    if (args.zoom) {
        zoom = args.zoom;
    }
    if (args.opacity) {
        opacity = args.opacity;
    }

    var mapnik = new OpenLayers.Layer.OSM();

    var layer = new OpenLayers.Layer.XYZ(
        "layer "+layer_name,
        "http://${s}.tile.openweathermap.org/map/"+layer_name+"/${z}/${x}/${y}.png",
        { isBaseLayer: false,
          opacity: opacity,
          sphericalMercator: true }
    );

    // Did not check argument type here, will do later
    var map = new OpenLayers.Map(mapId, { controls: [] });
    map.addControl(new OpenLayers.Control.Navigation());
    map.addLayers([mapnik, layer]);

    // Transform from WGS 1984
    var fromProjection = new OpenLayers.Projection("EPSG:4326");
    // to Spherical Mercator Projection
    var toProjection   = new OpenLayers.Projection("EPSG:900913");
    var center = new OpenLayers.LonLat(lon, lat).transform(fromProjection, toProjection);
    map.setCenter(center, zoom);
}