import { useEffect, useState } from "react";
import axios from "axios";
import CitySummary from "./CitySummary.tsx";
import {useSearchParams} from "react-router-dom";

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
        }
    }, [searchParams]);

    return (
        <div className="container city-search">
            <h2>Stadt suchen</h2>

            <input
                type="text"
                placeholder="z. B. Berlin, Paris, Dubai..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
            />

            {isLoading && <p>Lade Städte...</p>}

            <ul>
                {results.map((city) => (
                    <li
                        key={`${city.lat}-${city.lon}`}
                        className="city-list-item"
                        role="button"
                        tabIndex={0}
                        onClick={() => {
                            const cityNameOnly = city.display_name.split(",")[0];
                            setSelectedCity(cityNameOnly);
                        }}
                    >
                        <strong>{city.display_name}</strong>
                    </li>
                ))}
            </ul>

            {selectedCity && (
                <>
                    <hr />
                    <CitySummary cityName={selectedCity}  user={props.user} />
                </>
            )}
        </div>
    );
}
