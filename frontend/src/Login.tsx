const handleGitHubLogin = () => {
        window.open('/oauth2/authorization/github', '_self');
}
export default function Login() {

    return (
        <div className="login-container">
            <div className="login-form">
                <h2>Willkommen</h2>
                <p>Melden Sie sich an, um fortzufahren</p>
                <button className="github-login-button" onClick={handleGitHubLogin}>
                    Log in with GitHub
                </button>
            </div>
        </div>
    )
}