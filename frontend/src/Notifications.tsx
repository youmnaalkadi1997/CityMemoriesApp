import { useEffect, useState } from "react";
import axios from "axios";
import {Link, useNavigate} from "react-router-dom";

type NotificationType = {
    id: string;
    message: string;
    targetCity: string;
    commentId: string;
    read: boolean;
    createdAt : string;
};

type Props = {
    readonly user: string | undefined | null;
};

export default function Notifications({ user }: Props) {
    const [unreadCount, setUnreadCount] = useState<number>(0);
    const [notifications, setNotifications] = useState<NotificationType[]>([]);
    const navigate = useNavigate();

    const username = user;

    useEffect(() => {
        if (!username) return;

        axios.get("/api/notifications/count", { params: { username } })
            .then(res => setUnreadCount(res.data))
            .catch(console.error);

        axios.get("/api/notifications", { params: { username } })
            .then(res => setNotifications(res.data))
            .catch(console.error);
    }, [username]);

    function markNotificationAsRead(notificationId: string) {
        setNotifications(prev =>
            prev.map(n =>
                n.id === notificationId ? { ...n, read: true } : n
            )
        );
        setUnreadCount(prev => prev - 1);
    }

    function handleClick(notification: NotificationType) {
        axios
            .post(`/api/notifications/${notification.id}/read`)
            .then(() => markNotificationAsRead(notification.id))
            .catch(console.error);

        navigate(
            `/search?selected=${encodeURIComponent(notification.targetCity)}#comment-${notification.commentId}`
        );
    }

    return (
        <div>
            <div className="sidebar">
                <Link to={"/search"}>Suchen</Link>
                <Link to="/favorites">Favoritenliste</Link>
                <Link to= "/notifications" className="notification-link">Notifications {unreadCount > 0 && <span className="badgee">{unreadCount}</span>}</Link>
                <Link to="#" onClick={() => window.open('/logout', '_self')}>Logout</Link>
            </div>
            <div className="notifications">
                <div className="notifications-header">
                    <span>🔔 Notifications</span>
                </div>

                <div className="notification-list">
                    {notifications.map(n => (
                        <div
                            key={n.id}
                            role="button"
                            onClick={() => handleClick(n)}
                            className={`notification-card ${n.read ? "" : "unread"}`}
                        >
                            <p className="notification-message">{n.message}</p>
                            <div className="notification-date">
                                {new Date(n.createdAt).toLocaleString()}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}