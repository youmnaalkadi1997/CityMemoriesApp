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

type CityComment = {
    id: string;
    cityName: string;
    username: string;
    comment: string;
    imageUrl?: string;
    createdAt: string; // ISO string
    updatedAt: string;
};

type Props = {
    cityName: string;
    user: string |undefined|null
};

export default function CitySummary({ cityName, user }: Props) {
    const [data, setData] = useState<WikiData | null>(null);
    const [loading, setLoading] = useState(false);
    const [comment, setComment] = useState("");
    const [message, setMessage] = useState("");
    const [comments, setComments] = useState<CityComment[]>([]);


    useEffect(() => {
        if (!cityName) return;

        setLoading(true);

        const wikiUrl = `https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(cityName)}`;
        const commentsUrl = `/api/comment/${encodeURIComponent(cityName)}`;

        Promise.all([
            axios.get(wikiUrl),
            axios.get(commentsUrl)
        ])
            .then(([wikiRes, commentsRes]) => {
                setData(wikiRes.data);
                setComments(commentsRes.data);
            })
            .catch((error) => {
                console.error("Fehler beim Laden:", error);
                setData(null);
                setComments([]);
            })
            .finally(() => {
                setLoading(false);
            });

    }, [cityName]);

    if (loading) return <p>⏳ Lade Beschreibung...</p>;
    if (!data) return <p>❌ Keine Beschreibung gefunden.</p>;

    const addComment = () => {
        axios.post("/api/addcomment", {
            cityName: cityName,
            username: user,
            comment: comment
        })
            .then(() => {
                setMessage("✅ ");
                setComment("");
            })
            .catch((error) => {
                console.error("Error", error);
                setMessage("❌");
            });
    };

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

            <div style={{ marginTop: "20px" }}>
                <h4>Kommentieren ..</h4>
                <textarea
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    rows={4}
                    style={{ width: "100%", marginBottom: "10px" }}
                    placeholder="Kommentieren .."
                />
                <button onClick={addComment}>Senden</button>

                {message && (
                    <p style={{ marginTop: "10px", color: message.includes("✅") ? "green" : "red" }}>
                        {message}
                    </p>
                )}
            </div>
            <div style={{ marginTop: "30px" }}>
                <h4>🗨 Kommentare:</h4>
                {comments.length === 0 ? (
                    <p style={{ color: "gray" }}>Keine Kommentare für diese Stadt.</p>
                ) : (
                    <ul style={{ listStyle: "none", padding: 0 }}>
                        {comments.map((c) => (
                            <li key={c.id} style={{ marginBottom: "15px", borderBottom: "1px solid #ddd", paddingBottom: "10px" }}>
                                <strong>{c.username}</strong> schrieb am {new Date(c.createdAt).toLocaleString()}:
                                <p>{c.comment}</p>
                                {c.imageUrl && (
                                    <img src={c.imageUrl} alt="Bild zur Stadt" style={{ maxWidth: "100px", marginTop: "5px" }} />
                                )}
                            </li>
                        ))}
                    </ul>
                )}
            </div>

        </div>
    );
}
