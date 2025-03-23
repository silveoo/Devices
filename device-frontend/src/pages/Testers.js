import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const Testers = () => {
    const [testers, setTesters] = useState([]);

    useEffect(() => {
        api.get('/api/v1/testers')
            .then(res => setTesters(res.data))
            .catch(err => console.error(err));
    }, []);

    return (
        <div className="container">
            <h2 className="mb-4">Тестировщики</h2>
            <Link to="/testers/create" className="btn btn-success mb-3">Создать</Link>
            <table className="table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Имя</th>
                    <th>Логин</th>
                    <th>Роли</th>
                </tr>
                </thead>
                <tbody>
                {testers.map(tester => (
                    <tr key={tester.id}>
                        <td>{tester.id}</td>
                        <td>{tester.name}</td>
                        <td>{tester.username}</td>
                        <td>
                            {tester.roles.map(role => (
                                <div key={role.name}>
                                    {role.name} ({role.authorities.join(', ')})
                                </div>
                            ))}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default Testers;