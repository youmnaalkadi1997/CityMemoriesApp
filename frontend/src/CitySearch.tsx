import { useEffect, useState } from "react";
import axios from "axios";
import CitySummary from "./CitySummary.tsx";

type CityResult = {
    display_name: string;
    lat: string;
    lon: string;
};

export default function CitySearch() {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<CityResult[]>([]);
    const [selectedCity, setSelectedCity] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const fetchCities = (cityName: string) => {
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
    };

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

    return (
        <div style={{ maxWidth: "600px", margin: "0 auto" }}>
            <h2>🌍 Stadt suchen</h2>

            <input
                type="text"
                placeholder="z. B. Berlin, Paris, Beirut..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                style={{ width: "100%", padding: "8px", fontSize: "16px" }}
            />

            {isLoading && <p>⏳ Lade Städte...</p>}

            <ul>
                {results.map((city, index) => (
                    <li
                        key={index}
                        style={{ margin: "10px 0", cursor: "pointer", color: "blue" }}
                        onClick={() => {
                            const cityNameOnly = city.display_name.split(",")[0]; // فقط الاسم الأول
                            setSelectedCity(cityNameOnly);
                        }}
                    >
                        <strong>{city.display_name}</strong>
                        <br />
                        📍 Lat: {city.lat}, Lon: {city.lon}
                    </li>
                ))}
            </ul>

            {selectedCity && (
                <>
                    <hr />
                    <CitySummary cityName={selectedCity} />
                </>
            )}
        </div>
    );
}
