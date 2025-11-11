import { useEffect, useState } from "react";
import axios from "axios";
import { Link, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import CitySummary from "./CitySummary.tsx";
import { ClipLoader } from "react-spinners";

type CityResult = {
    display_name: string;
    lat: string;
    lon: string;
};

type ProtectedRoutProps = {
    user: string | undefined | null;
};

function logout() {
    window.open("/logout", "_self");
}

export default function CitySearch(props: Readonly<ProtectedRoutProps>) {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<CityResult[]>([]);
    const [selectedCity, setSelectedCity] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [popularCities, setPopularCities] = useState<
        { cityName: string; favoritesCount: number; firstCommentPhoto?: string; commentsCount: number }[]
    >([]);
    const location = useLocation();
    const [searchHistory, setSearchHistory] = useState<string[]>([]);
    const [unreadCount, setUnreadCount] = useState<number>(0);
    const username = props.user;

    function getSearchHistory() {
        if (!props.user) return;
        axios
            .get(`/api/searchHistory/${props.user}`)
            .then((res) => setSearchHistory(res.data))
            .catch((err) => console.error("Fehler beim Laden der Suchhistorie:", err));
    }

    function addToHistory(cityName: string) {
        if (!props.user) return;
        axios
            .post(`/api/searchHistory/${props.user}`, cityName, {
                headers: { "Content-Type": "text/plain" },
            })
            .then(() => setSearchHistory([]))
            .catch((err) => console.error("Fehler beim Hinzufügen zur Suchhistorie:", err));
    }

    function handleCitySelect(cityName: string, lat?: string, lon?: string) {
        addToHistory(cityName);
        setSearchHistory([]);
        setQuery("");
        setResults([]);
        setPopularCities([]);
        navigate(`/search?selected=${encodeURIComponent(cityName)}${lat && lon ? `&lat=${lat}&lon=${lon}` : ""}`);
    }

    function fetchCities(cityName: string) {
        setIsLoading(true);
        axios
            .get("https://nominatim.openstreetmap.org/search", {
                params: {
                    q: cityName,
                    format: "json",
                    addressdetails: 1,
                    limit: 5,
                },
                headers: {
                    "User-Agent": "CityMemoriesApp/1.0",
                },
            })
            .then((response) => {
                setResults(response.data);
            })
            .catch((error) => {
                console.error("Fehler beim Abrufen:", error);
            })
            .finally(() => {
                setIsLoading(false);
            });
    }

    function handleCitySelectWithCoords(city: string) {
        axios
            .get("https://nominatim.openstreetmap.org/search", {
                params: { q: city, format: "json", limit: 1 },
                headers: { "User-Agent": "CityMemoriesApp/1.0" },
            })
            .then((res) => {
                const result = res.data[0];
                if (result) {
                    handleCitySelect(city, result.lat, result.lon);
                } else {
                    handleCitySelect(city);
                }
            })
            .catch(() => handleCitySelect(city));
    }



    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            const target = event.target as HTMLElement;
            if (!target.closest(".search-container")) {
                setResults([]);
                setSearchHistory([]);
            }
        }
        document.addEventListener("click", handleClickOutside);
        return () => {
            document.removeEventListener("click", handleClickOutside);
        };
    }, []);

    useEffect(() => {
        if (query.length === 0 && !selectedCity) {
            axios
                .get("/api/mostPopularCities")
                .then((res) => setPopularCities(res.data))
                .catch((err) => console.error(err));
        }
    }, [query, location.pathname, selectedCity]);

    useEffect(() => {
        if (query.length < 2) {
            setResults([]);
            return;
        }

        const delay = setTimeout(() => {
            fetchCities(query);
        }, 400);

        return () => clearTimeout(delay);
    }, [query]);

    useEffect(() => {
        const selected = searchParams.get("selected");
        if (selected) {
            setSelectedCity(selected);
        } else {
            setSelectedCity(null);
        }
    }, [searchParams]);

    useEffect(() => {
        if (!username) return;
        axios.get("/api/notifications/count", { params: { username } })
            .then(res => setUnreadCount(res.data))
            .catch(console.error);
    }, [username]);

    return (
        <div className="container city-search">
            <div className="sidebar">
                <Link
                    to={"/search"}
                    onClick={() => {
                        setQuery("");
                        setSelectedCity(null);
                        setResults([]);
                        setSearchHistory([]);
                    }}
                >
                    Suchen
                </Link>
                <Link to="/favorites">Favoritenliste</Link>
                <Link to= "/notifications" className="notification-link">Notifications {unreadCount > 0 && <span className="badgee">{unreadCount}</span>}</Link>
                <Link to="#" onClick={logout}>
                    Logout
                </Link>
            </div>

            <div className="search-container">
                <input
                    type="text"
                    className="search-input"
                    placeholder="z. B. Berlin, Paris, Dubai..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    onFocus={getSearchHistory}
                />

                {searchHistory.length > 0 && query.length === 0 && (
                    <ul className="search-history">
                        {searchHistory.map((city) => (
                            <li key={city}>
                                <button
                                    type="button"
                                    onClick={() => handleCitySelectWithCoords(city)}
                                    className="search-history-item"
                                    style={{
                                        all: "unset",
                                        cursor: "pointer",
                                        display: "block",
                                        width: "100%",
                                        textAlign: "left",
                                    }}
                                >
                                    {city}
                                </button>
                            </li>
                        ))}
                    </ul>
                )}

                {results.length > 0 && (
                    <ul className="search-results">
                        {results.map((city) => (
                            <li key={`${city.lat}-${city.lon}`}>
                                <button
                                    className="city-list-item"
                                    onClick={() => {
                                        handleCitySelect(city.display_name.split(",")[0], city.lat, city.lon);
                                    }}
                                >
                                    {city.display_name}
                                </button>
                            </li>
                        ))}
                    </ul>
                )}
                {isLoading && (
                    <div className="spinner-wrapper">
                        <ClipLoader color="#36d7b7" size={40} />
                    </div>
                )}
            </div>
            <div className="search-container">

                {query.length === 0 && !selectedCity && popularCities.length > 0 && (
                    <div className="popular-cities-row">
                        {popularCities.map((city) => (
                            <div
                                key={city.cityName}
                                className="popular-city-card"
                                tabIndex={0}
                                onKeyDown={(e) => {
                                    if (e.key === "Enter" || e.key === " ") {
                                        handleCitySelectWithCoords(city.cityName);
                                    }
                                }}
                                onClick={() => handleCitySelectWithCoords(city.cityName)}
                            >
                                <img
                                    src={
                                        city.firstCommentPhoto
                                            ? city.firstCommentPhoto
                                            : `https://via.placeholder.com/200x140?text=${encodeURIComponent(city.cityName)}`
                                    }
                                    alt={city.cityName}
                                />
                                <div className="city-card-info">
                                    <h4>{city.cityName}</h4>
                                    <span className="likes">❤️ {city.favoritesCount}</span>
                                    <span className="comments">💬 {city.commentsCount}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {selectedCity && (
                <>
                    <hr />
                    <CitySummary cityName={selectedCity} user={props.user} />
                </>
            )}
        </div>
    );
}
