import { useEffect, useState } from "react";
import axios from "axios";
import {Link, useNavigate} from "react-router-dom";

type FavoriteGroup = {
    name: string;
    cities: string[];
};

type Props = {
    user: string | undefined | null;
};

export default function FavoutiteListe({ user }: Props) {
    const [favorites, setFavorites] = useState<string[]>([]);
    const [groups, setGroups] = useState<FavoriteGroup[]>([]);
    const [showNewGroupModal, setShowNewGroupModal] = useState(false);
    const [newGroupName, setNewGroupName] = useState("");
    const [selectedGroup, setSelectedGroup] = useState<FavoriteGroup | null>(null);
    const [selectedCities, setSelectedCities] = useState<string[]>([]);
    const navigate = useNavigate();

    useEffect(() => {
        if (!user) return;

        axios.get("/api/favorites", { params: { username: user } })
            .then(res => setFavorites(res.data))
            .catch(err => console.error(err));

        axios.get("/api/groups", { params: { username: user } })
            .then(res => setGroups(res.data))
            .catch(err => console.error(err));
    }, [user]);

    function createGroup() {
        if (!user || newGroupName.trim() === "") return;

        axios.post("/api/groups", null, { params: { username: user, groupName: newGroupName } })
            .then(res => {
                setGroups(prev => [...prev, res.data]);
                setNewGroupName("");
                setShowNewGroupModal(false);
            })
            .catch(err => console.error(err));
    }

    function openGroupModal(group: FavoriteGroup) {
        setSelectedGroup(group);
        setSelectedCities(group.cities);
    }

    function toggleCitySelection(city: string) {
        setSelectedCities(prev =>
            prev.includes(city)
                ? prev.filter(c => c !== city)
                : [...prev, city]
        );
    }

    function saveCities() {
        if (!user || !selectedGroup) return;

        const promises = selectedCities
            .filter(city => !selectedGroup.cities.includes(city))
            .map(city => axios.post("/api/addCity", null, { params: { username: user, groupName: selectedGroup.name, city } }));

        Promise.all(promises)
            .then(() => {
                setGroups(prev =>
                    prev.map(g =>
                        g.name === selectedGroup.name
                            ? { ...g, cities: selectedCities }
                            : g
                    )
                );
                setSelectedGroup(null);
                setSelectedCities([]);
            })
            .catch(err => console.error(err));
    }

    function deleteGroup(groupName: string) {
        if (!user) return;
        if (!window.confirm("Willst du diese Gruppe wirklich löschen?")) return;

        axios.delete("/api/groups", { params: { username: user, groupName } })
            .then(() => {
                setGroups(prev => prev.filter(g => g.name !== groupName));
            })
            .catch(err => console.error(err));
    }

    return (
        <div>
            <div className="sidebar">
                <Link to={"/search"}>Suchen</Link>
                <Link to="/favorites">Favoritenliste</Link>
                <Link to= "/notifications">Notifications</Link>
                <Link to="#" onClick={() => window.open('/logout', '_self')}>Logout</Link>
            </div>
            <div className="content">

            <button className="new-group-btn" onClick={() => setShowNewGroupModal(true)}>+ Neue Gruppe</button>
                <hr className="separator" />

            {showNewGroupModal && (
                <div className="modal-backdrop">
                    <div className="modal-content">
                        <h3>Neue Gruppe erstellen</h3>
                        <input
                            type="text"
                            value={newGroupName}
                            placeholder="Gruppenname"
                            onChange={e => setNewGroupName(e.target.value)}
                        />
                        <div style={{ marginTop: 10 }}>
                            <button onClick={createGroup}>Speichern</button>
                            <button onClick={() => setShowNewGroupModal(false)} style={{ marginLeft: 10 }}>Abbrechen</button>
                        </div>
                    </div>
                </div>
            )}

            <h2 className="favorites-title">Meine Favoriten-Städte</h2>

            <div className="favorites-grid">
                {favorites.map(city => (
                    <div
                        key={city}
                        className="favorite-card"
                        onClick={() => navigate(`/search?selected=${encodeURIComponent(city)}`)}
                    >
                        {city}
                    </div>
                ))}
            </div>
                <hr className="separator" />

            <h2 className="favorites-title">Meine-Gruppen</h2>

            <div className="groups-grid" style={{ display: "flex", gap: "10px", flexWrap: "wrap", marginTop: 20 }}>
                {groups.map(group => (
                    <div key={group.name} className="group-card" style={{ border: "1px solid gray", padding: "10px", borderRadius: "6px", minWidth: "150px" }}>
                        <h4>{group.name}</h4>
                        <p>{group.cities.join(", ") || "Keine Städte"}</p>
                        <button onClick={() => openGroupModal(group)}>🖊️ Bearbeiten</button>
                        <button onClick={() => deleteGroup(group.name)} style={{ marginLeft: 5 }}>🗑️</button>
                    </div>
                ))}
            </div>

            {selectedGroup && (
                <div className="modal-backdrop">
                    <div className="modal-content">
                        <h3>Städte für {selectedGroup.name} auswählen</h3>
                        <div style={{ display: "flex", flexDirection: "column", maxHeight: "300px", overflowY: "auto" }}>
                            {favorites.map(city => (
                                <label key={city} style={{ margin: "4px 0" }}>
                                    <input
                                        type="checkbox"
                                        checked={selectedCities.includes(city)}
                                        onChange={() => toggleCitySelection(city)}
                                    />
                                    {city}
                                </label>
                            ))}
                        </div>
                        <div style={{ marginTop: 10 }}>
                            <button onClick={saveCities}>Speichern</button>
                            <button onClick={() => setSelectedGroup(null)} style={{ marginLeft: 10 }}>Abbrechen</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
        </div>
    );
}