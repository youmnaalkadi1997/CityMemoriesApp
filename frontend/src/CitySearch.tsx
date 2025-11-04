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
            .then((res) => setSearchHistory(res.data))
            .catch((err) => console.error("Fehler beim Hinzufügen zur Suchhistorie:", err));
    }

    function handleCitySelect(cityName: string) {
        addToHistory(cityName);
        setQuery("");
        setResults([]);
        setSearchHistory([]);
        setPopularCities([]);
        navigate(`/search?selected=${encodeURIComponent(cityName)}`);
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

    function logout() {
        window.open("/logout", "_self");
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
                <Link to= "/notifications">Notifications</Link>
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
                        {searchHistory.map((city, index) => (
                            <li key={index} onClick={() => handleCitySelect(city)}>
                                {city}
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
                                        const cityNameOnly = city.display_name.split(",")[0];
                                        handleCitySelect(cityNameOnly);
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
                                onClick={() => handleCitySelect(city.cityName)}
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
