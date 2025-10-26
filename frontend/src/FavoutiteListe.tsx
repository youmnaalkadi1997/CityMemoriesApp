import {useEffect, useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import axios from "axios";



type Props = {
    user: string | undefined | null;
};
export default function FavoutiteListe({ user }: Props){

    const [favorites, setFavorites] = useState<string[]>([]);
    const navigate = useNavigate();

    useEffect(() => {
        if (!user) return;

        axios.get("/api/favorites", { params: { username: user } })
            .then(res => setFavorites(res.data))
            .catch(err => console.error(err));
    }, [user]);

    function logout() {
        window.open('/logout', '_self');
    }

    return (
        <div>
            <div className="sidebar">
                <Link to={"/search"}>Suchen</Link>
                <Link to="/favorites">Favoritenliste</Link>
                <Link to="#" onClick={logout}>Logout</Link>
            </div>
            <h2>Deine Lieblingsstädte</h2>
            {favorites.length === 0 ? (
                <p>Du hast noch keine Städte zu deinen Favoriten hinzugefügt</p>
            ) : (
                <div className="favorites-grid">
                    {favorites.map((fav) => (
                        <button key={fav} className="favorite-card"
                            onClick={() => navigate(`/search?selected=${encodeURIComponent(fav)}`)}
                        >
                            {fav}
                        </button>
                    ))}
                </div>
            )}
        </div>
    );

}