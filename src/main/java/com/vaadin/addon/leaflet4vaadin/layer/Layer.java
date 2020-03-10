// Copyright 2020 Gabor Kokeny and contributors
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.vaadin.addon.leaflet4vaadin.layer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.addon.leaflet4vaadin.LeafletMap;
import com.vaadin.addon.leaflet4vaadin.layer.events.Evented;
import com.vaadin.addon.leaflet4vaadin.layer.events.LeafletEvent;
import com.vaadin.addon.leaflet4vaadin.layer.events.LeafletEventListener;
import com.vaadin.addon.leaflet4vaadin.layer.events.PopupEvent;
import com.vaadin.addon.leaflet4vaadin.layer.events.TooltipEvent;
import com.vaadin.addon.leaflet4vaadin.layer.events.types.LeafletEventType;
import com.vaadin.addon.leaflet4vaadin.layer.groups.LayerGroup;
import com.vaadin.addon.leaflet4vaadin.layer.ui.popup.Popup;
import com.vaadin.addon.leaflet4vaadin.layer.ui.tooltip.Tooltip;

/**
 * A set of methods from the Layer base class that all Leaflet layers use.
 * Inherits all methods, options and events from L.Evented.
 * 
 * @author <strong>Gabor Kokeny</strong> Email:
 *         <a href='mailto=kokeny19@gmail.com'>kokeny19@gmail.com</a>
 * @since 2020-02-06
 * @version 1.0
 */
@JsonIgnoreProperties(value = { "json" })
public abstract class Layer implements Evented, Serializable {

	private static final long serialVersionUID = -1803411573095089760L;

	public static enum Events implements LeafletEventType {
		add, remove, tooltipopen, tooltipclose, popupopen, popupclose;
	}

	private transient final Map<LeafletEventType, Set<LeafletEventListener>> eventListeners = new HashMap<>();

	private transient final ObjectMapper objectMapper = new ObjectMapper();

	public static final String DEFAULT_PANE = "overlayPane";
	private String uuid;
	private String name;
	private String json;
	private String pane = DEFAULT_PANE;
	private String attribution;
	private Popup popup;
	private Tooltip tooltip;
	private List<String> events = new ArrayList<>();

	protected Layer() {
		this.uuid = UUID.randomUUID().toString();
		configureObjectMapper(objectMapper);
	}

	protected void configureObjectMapper(final ObjectMapper objectMapper) {
	}

	/**
	 * Fired after the layer is added to a map
	 * 
	 * @param listener the event listener
	 */
	public void onAdd(LeafletEventListener<LeafletEvent> listener) {
		on(Events.add, listener);
	}

	/**
	 * Fired after the layer is removed from a map
	 * 
	 * @param listener the event listener
	 */
	public void onRemove(LeafletEventListener<LeafletEvent> listener) {
		on(Events.remove, listener);
	}

	/**
	 * Fired when a tooltip bound to this layer is opened.
	 * 
	 * @param listener the event listener
	 */
	public void onTooltipOpen(LeafletEventListener<TooltipEvent> listener) {
		on(Events.tooltipopen, listener);
	}

	/**
	 * Fired when a tooltip bound to this layer is closed.
	 * 
	 * @param listener the event listener
	 */
	public void onTooltipClose(LeafletEventListener<TooltipEvent> listener) {
		on(Events.tooltipclose, listener);
	}

	/**
	 * Fired when a popup bound to this layer is opened
	 * 
	 * @param listener the event listener
	 */
	public void onPopupOpen(LeafletEventListener<PopupEvent> listener) {
		on(Events.popupopen, listener);
	}

	/**
	 * Fired when a popup bound to this layer is closed
	 * 
	 * @param listener the event listener
	 */
	public void onPopupClose(LeafletEventListener<PopupEvent> listener) {
		on(Events.popupclose, listener);
	}

	@Override
	public <T extends LeafletEvent> void addEventListener(LeafletEventType eventType,
			LeafletEventListener<T> listener) {
		if (!events.contains(eventType.getLeafletEvent())) {
			events.add(eventType.getLeafletEvent());
		}
		Set<LeafletEventListener> listeners = eventListeners.get(eventType);
		if (listeners == null) {
			listeners = new HashSet<>();
			eventListeners.putIfAbsent(eventType, listeners);
		}
		listeners.add(listener);
	}

	public <T extends LeafletEvent> void fireEvent(T leafletEvent) {
		Optional<LeafletEventType> event = eventListeners.keySet().stream()
				.filter(type -> type.equals(leafletEvent.getType())).findFirst();

		if (event.isPresent()) {
			eventListeners.get(event.get()).forEach(listener -> listener.handleEvent(leafletEvent));
		}
	}

	public void addTo(LayerGroup layerGroup) {
		layerGroup.addLayer(this);
	}

	public void addTo(LeafletMap leafletMap) {
		leafletMap.addLayer(this);
	}

	public Optional<Layer> findLayer(String layerId) {
		return itsMe(layerId) ? Optional.of(this) : Optional.empty();
	}

	private boolean itsMe(String layerId) {
		return this.getUuid().equals(layerId);
	}

	public String getLeafletType() {
		return getClass().getSimpleName();
	}

	public List<String> getEvents() {
		return this.events;
	}

	public String getAttribution() {
		return this.attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public String getUuid() {
		return uuid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPane() {
		return this.pane;
	}

	public void setPane(String pane) {
		this.pane = pane;
	}

	public Popup getPopup() {
		return this.popup;
	}

	public void bindPopup(String popupContent) {
		bindPopup(new Popup(popupContent));
	}

	public void bindPopup(Popup popupOptions) {
		this.popup = popupOptions;
	}

	public Tooltip getTooltip() {
		return this.tooltip;
	}

	public void bindTooltip(String tooltipContent) {
		bindTooltip(new Tooltip(tooltipContent));
	}

	public void bindTooltip(Tooltip tooltipOptions) {
		this.tooltip = tooltipOptions;
	}

	public String getJson() {
		if (this.json == null) {
			try {
				this.json = objectMapper.writeValueAsString(this);
				return this.json;
			} catch (IOException e) {
				throw new RuntimeException("Unable to convert Layer into JSON type", e);
			}
		}
		return this.json;
	}

	/**
	 * @param json the json to set
	 */
	public void setJson(String json) {
		this.json = json;
	}

}
