import {type ChangeEvent, useEffect, useState} from "react";
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
    createdAt: string;
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
    const [image, setImage] = useState<File>();
    const [favMessage, setFavMessage] = useState("");
    const [isFavorite, setIsFavorite] = useState(false);



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
            setFavMessage("");
        }, 3000);

        return () => clearTimeout(timer);
    }, [message, favMessage]);

    useEffect(() => {
        if (!user) return;

        axios.get("/api/favorites",{params : {username : user}})
            .then(res => {
                const favoriteCities: string[] = res.data;
                setIsFavorite(favoriteCities.includes(cityName));
            })
            .catch(err => console.error(err));
    }, [cityName, user]);

    function addComment(event?: React.FormEvent<HTMLFormElement>) {
        event?.preventDefault();
        const data = new FormData();

        if (image) {
            data.append("file", image);
        }

        const json = {
            cityName: cityName,
            username: user,
            comment: comment
        };

        data.append("data", new Blob([JSON.stringify(json)], { type: "application/json" }));

        axios.post("/api/addcomment", data, {
            headers: { "Content-Type": "multipart/form-data" }
        })
            .then(() => {
                setMessage("Kommentar erfolgreich hinzugefügt");
                setComment("");
                setImage(undefined);
                return axios.get(`/api/comment/${encodeURIComponent(cityName)}`);
            })
            .then(res => setComments(res.data))
            .catch((error) => {
                if (error.response) {
                    setMessage(error.response.data?.message || "Fehler beim Hochladen");
                } else {
                    setMessage("Netzwerkfehler oder Server nicht erreichbar");
                }
            });
    }

    function onFileChange(event: ChangeEvent<HTMLInputElement>) {
        if (event.target.files) {
            setImage(event.target.files[0])
        }
    }

    function toggleFavorite() {
        if (!user) {
            setFavMessage("Bitte einloggen, um die Stadt zu favorisieren.");
            return;
        }

        if (isFavorite) {
            axios.delete(`/api/deleteFromFav/${encodeURIComponent(cityName)}` ,{ params : {username : user}})
                .then(() => {
                    setIsFavorite(false);
                    setFavMessage("Stadt aus Favoriten entfernt!");
                })
                .catch(err => {
                    console.error(err);
                    setFavMessage("Fehler beim Entfernen aus Favoriten.");
                });
        } else {
            axios.post("/api/addToFavorites", {
                cityName: cityName,
                username: user
            })
                .then(() => {
                    setIsFavorite(true);
                    setFavMessage("Stadt zur Favoritenliste hinzugefügt!");
                })
                .catch(err => {
                    console.error(err);
                    setFavMessage("Fehler beim Hinzufügen zur Favoritenliste.");
                });
        }
    }

    if (loading) return <p>Lade Beschreibung...</p>;
    if (!data) return <p>Keine Beschreibung gefunden.</p>;

    return (
        <div className="city-summary">
            <h3>{data.title}</h3>
            <button className="button" onClick={toggleFavorite}>
                {isFavorite ? "In Favoriten" : "Zu Favoriten hinzufügen"}
            </button>
            {favMessage && <p>{favMessage}</p>}

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

            <form className="form" onSubmit={addComment}>
                <div className="textarea-wrapper">
                <textarea
                    value={comment}
                    className="input"
                    onChange={(e) => setComment(e.target.value)}
                    rows={4}
                    placeholder="Kommentieren .."
                />
                <input
                    type="file"
                    accept="image/*"
                    id="fileInput"
                    style={{ display: "none" }}
                    onChange={onFileChange}
                />
                <label htmlFor="fileInput" className="camera-button">📷</label>
                </div>
                <button className="button" type="submit">Senden</button>
                {message && (
                    <p>
                        {message}
                    </p>
                )}
            </form>
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
                                        <button
                                            onClick={(e) => {
                                                e.preventDefault();
                                                const confirmDelete = window.confirm(
                                                    "Bist du sicher, dass du diesen Kommentar löschen möchtest?"
                                                );
                                                if (confirmDelete) {
                                                    window.location.href = `/delete/${c.id}?city=${encodeURIComponent(cityName)}`;
                                                }
                                            }}
                                        >
                                            Löschen
                                        </button>
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
