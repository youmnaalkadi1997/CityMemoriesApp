import { useEffect, useState } from "react";
import axios from "axios";
import {Link} from "react-router-dom";

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
    readonly  cityName: string;
    readonly  user: string |undefined|null
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

        axios.get(`https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(cityName)}`)
            .then((res) => setData(res.data))
            .catch((err) => {
                console.error("Wiki Fehler", err);
                setData(null);
            });

        axios.get(`/api/comment/${encodeURIComponent(cityName)}`)
            .then((res) => setComments(res.data))
            .catch((err) => {
                console.error("Comment Fehler", err);
                setComments([]);
            })
            .finally(() => setLoading(false));

    }, [cityName]);

    useEffect(() => {
        const timer = setTimeout(() => {
            setMessage("");
        }, 3000);

        return () => clearTimeout(timer);
    }, [message]);

    function addComment () {
        axios.post("/api/addcomment", {
            cityName: cityName,
            username: user,
            comment: comment
        })
            .then(() => {
                setMessage("Kommentar erfolgreich hinzugefügt");
                setComment("");
                return axios.get(`/api/comment/${encodeURIComponent(cityName)}`);
            })
            .then(res => setComments(res.data))
            .catch((error) => {
                if (error.response) {
                    const serverMessage = error.response.data?.message || "Unbekannter Fehler";
                    setMessage(serverMessage);
                } else {
                    setMessage("Netzwerkfehler oder Server nicht erreichbar");
                }
            });
    }
    if (loading) return <p>Lade Beschreibung...</p>;
    if (!data) return <p>Keine Beschreibung gefunden.</p>;

    return (
        <div className="city-summary">
            <h3>{data.title}</h3>

            {data.thumbnail && (
                <img src={data.thumbnail.source} alt={data.title} width="100%" />
            )}

            <p>{data.extract}</p>

            {data.content_urls?.desktop.page && (
                <p>
                    Quelle:{" "}
                    <a href={data.content_urls.desktop.page} target="_blank" rel="noopener noreferrer">
                        Wikipedia – {data.title}
                    </a>
                </p>
            )}

            <div>
                <textarea
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    rows={4}
                    placeholder="Kommentieren .."
                />
                <button onClick={addComment}>Senden</button>

                {message && (
                    <p>
                        {message}
                    </p>
                )}
            </div>
            <div>
                <h4>Kommentare:</h4>
                {comments.length === 0 ? (
                    <p>Keine Kommentare für diese Stadt.</p>
                ) : (
                    <ul>
                        {comments.map((c) => (
                            <li key={c.id} className="comment-box">
                                <strong>{c.username}</strong> schrieb am {new Date(c.createdAt).toLocaleString()}:
                                <p>{c.comment}</p>
                                {c.updatedAt && c.updatedAt !== c.createdAt && (
                                    <p>
                                        Zuletzt bearbeitet am: {new Date(c.updatedAt).toLocaleString()}
                                    </p>
                                )}

                                {c.imageUrl && <img src={c.imageUrl} alt="Bild zur Stadt" />}

                                {user === c.username && (
                                    <div className="link-buttons">
                                        <Link to={`/edit/${c.id}?city=${encodeURIComponent(cityName)}`}>
                                            <button>Bearbeiten</button>
                                        </Link>
                                        <Link to={`/delete/${c.id}?city=${encodeURIComponent(cityName)}`}>
                                            <button>Löschen</button>
                                        </Link>
                                    </div>
                                )}
                            </li>
                        ))}
                    </ul>
                )}
            </div>

        </div>
    );
}
