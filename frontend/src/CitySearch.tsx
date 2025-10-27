import { useEffect, useState } from "react";
import axios from "axios";
import {Link, useLocation, useNavigate, useSearchParams} from "react-router-dom";
import CitySummary from "./CitySummary.tsx";
import { ClipLoader } from "react-spinners";

type CityResult = {
    display_name: string;
    lat: string;
    lon: string;
};

type ProtectedRoutProps = {
    user: string |undefined|null
}

export default function CitySearch(props:Readonly<ProtectedRoutProps>) {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<CityResult[]>([]);
    const [selectedCity, setSelectedCity] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [popularCities, setPopularCities] = useState<{ cityName: string; favoritesCount: number }[]>([]);
    const location = useLocation();


    function fetchCities  (cityName: string)  {
        setIsLoading(true);
        axios.get("https://nominatim.openstreetmap.org/search", {
            params: {
                q: cityName,
                format: "json",
                addressdetails: 1,
                limit: 5,
            },
            headers: {
                "User-Agent": "CityMemoriesApp/1.0"
            }
        })
            .then(response => {
                setResults(response.data);
            })
            .catch(error => {
                console.error("Fehler beim Abrufen:", error);
            })
            .finally(() => {
                setIsLoading(false);
            });
    }

    function logout() {
        window.open('/logout', '_self');
    }

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            const target = event.target as HTMLElement;
            if (!target.closest('.search-container')) {
                setResults([]);
            }
        }
        document.addEventListener('click', handleClickOutside);
        return () => {
            document.removeEventListener('click', handleClickOutside);
        };
    }, []);
    useEffect(() => {
        if (query.length === 0 && !selectedCity) {
            axios.get("/api/mostPopularCities")
                .then(res => setPopularCities(res.data))
                .catch(err => console.error(err));
        }
    }, [query , location.pathname , selectedCity]);

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
        }else {
            setSelectedCity(null);
        }
    }, [searchParams]);

    return (

        <div className="container city-search">
            <div className="sidebar">
                <Link to={"/search"} onClick={() => {
                    setQuery("");
                    setSelectedCity(null);
                    setResults([]);
                }}>Suchen</Link>
                <Link to="/favorites">Favoritenliste</Link>
                <Link to="#" onClick={logout}>Logout</Link>
            </div>
            <h2>Stadt suchen</h2>

            <div className="search-container">
            <input
                type="text"
                className="search-input"
                placeholder="z. B. Berlin, Paris, Dubai..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
            />
                {isLoading && (
                    <div className="spinner-wrapper">
                        <ClipLoader color="#36d7b7" size={50} />
                    </div>
                )}
                {query.length === 0 && !selectedCity && popularCities.length > 0 && (
                    <div className="most-popular">
                        <h3>Beliebte Städte</h3>
                        <div className="popular-cities">
                            {popularCities.map((city) => (
                                <button
                                    key={city.cityName}
                                    className="popular-city-button"
                                    onClick={() => {
                                        navigate(`/search?selected=${encodeURIComponent(city.cityName)}`);
                                        setPopularCities([]);
                                    }}
                                >
                                    {city.cityName} <span className="badge">{city.favoritesCount}</span>
                                </button>
                            ))}
                        </div>
                    </div>
                )}

            <ul>
                <div className={`search-results ${results.length > 0 ? 'visible' : ''}`}>
                {results.map((city) => (
                    <li key={`${city.lat}-${city.lon}`}>
                        <button
                            className="city-list-item"
                            onClick={() => {
                                const cityNameOnly = city.display_name.split(",")[0];
                                navigate(`/search?selected=${encodeURIComponent(cityNameOnly)}`);
                                //setSelectedCity(cityNameOnly);
                                setResults([]);
                                setPopularCities([]);
                            }}
                        >
                            {city.display_name}
                        </button>
                    </li>
                ))}
                </div>
            </ul>
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