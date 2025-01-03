[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

# Trip Rover

Trip Rover is a collaborative travel planning tool that allows users to create and manage travel plans with multiple participants.

## Features

- Create and manage travel plans with multiple participants
- Real-time collaborative updates on plan places (create new place, update place, update place stay duration, delete place, reorder place through drag-and-drop)
- Role-based access control on plan (owner, editor, viewer)

### Backend (`/backend`)
Spring Boot application providing REST and WebSocket services:
- [Backend Documentation](backend/README.md)

### Infrastructure (`/terraform`)
AWS infrastructure managed with Terraform:
- [Infrastructure Documentation](terraform/README.md)

## License
This project is licensed under the GNU Affero General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
