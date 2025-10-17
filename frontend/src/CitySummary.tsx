import { useEffect, useState } from "react";
import axios from "axios";

type WikiData = {
    title: string;
    extract: string;
    thumbnail?: {
        source: string;
        width: number;
        height: number;
    };
    content_urls?: {
        desktop: {
            page: string;
        };
    };
};

export default function CitySummary({ cityName }: { cityName: string }) {
    const [data, setData] = useState<WikiData | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!cityName) return;

        setLoading(true);

        axios.get(`https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(cityName)}`)
            .then(res => {
                setData(res.data);
            })
            .catch(err => {
                console.error("Fehler beim Laden von Wikipedia:", err);
                setData(null);
            })
            .finally(() => {
                setLoading(false);
            });

    }, [cityName]);

    if (loading) return <p>⏳ Lade Beschreibung...</p>;
    if (!data) return <p>❌ Keine Beschreibung gefunden.</p>;

    return (
        <div style={{ marginTop: "20px" }}>
            <h3>{data.title}</h3>

            {data.thumbnail && (
                <img src={data.thumbnail.source} alt={data.title} width="100%" />
            )}

            <p>{data.extract}</p>

            {data.content_urls?.desktop.page && (
                <p style={{ fontSize: "12px", color: "gray" }}>
                    Quelle:{" "}
                    <a href={data.content_urls.desktop.page} target="_blank" rel="noopener noreferrer">
                        Wikipedia – {data.title}
                    </a>
                </p>
            )}
        </div>
    );
}
