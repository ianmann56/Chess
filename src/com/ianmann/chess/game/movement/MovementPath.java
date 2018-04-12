/**
 * @TODO: TODO
 *
 * @author Ian
 * Created: Apr 8, 2018
 */
package com.ianmann.chess.game.movement;

import java.time.chrono.IsoChronology;
import java.util.ArrayList;
import java.util.LinkedList;

import com.ianmann.chess.game.TeamColor;

/**
 * <p>
 * Represents a collection of {@link Square}s that a piece can
 * move through to get to it's destination.
 * </p>
 *
 * @author Ian
 * Created: Apr 8, 2018
 *
 */
public class MovementPath extends LinkedList<Square> {
	
	/**
	 * The starting point of this path.
	 */
	private Square root;
	
	/**
	 * The forward orientation of this path.
	 */
	private Orientation orientation;
	
	/**
	 * <p>
	 * Represents whether or not this path is a continuous path
	 * (movement can end at any point along this path) or a
	 * destination path (movement can only end at the end of this
	 * path).
	 * </p>
	 * <p>
	 * If true, this is a continuous path, otherwise, this is a
	 * destination path.
	 * </p>
	 */
	private boolean isContinuous;
	
	/**
	 * The team that this path is relative to. This is used to ensure that the path does
	 * not allow the piece following it to end on squares with pieces of their own team.
	 */
	private TeamColor team;
	
	/**
	 * Instantiate an abstraction of a path of movement that a
	 * game piece can take.
	 * @param _root
	 * @param _orientation
	 * @param _isContinuous Represents whether or not this path is a continuous path
	 * (movement can end at any point along this path) or a
	 * destination path (movement can only end at the end of this
	 * path).
	 * @param _team The team that this path is relative to.
	 */
	public MovementPath(Square _root, Orientation _orientation, boolean _isContinuous, TeamColor _team) {
		this.root = _root;
		this.orientation = _orientation;
		this.isContinuous = _isContinuous;
		this.team = _team;
	}
	
	/**
	 * <p>
	 * Adds up to [_length] number of squares in the given two direction of this square.
	 * The number of squares that were added is returned. There may not be _length
	 * number of squares in this direction due to the edge of the board.
	 * </p>
	 * <p>
	 * The number returned skips will only include squares along the diagonal path, and
	 * not the vertical/horizontal path squares.
	 * </p>
	 * @param length
	 * @return
	 */
	public int buildDiagonal(Direction _sideWays, Direction _verticle, int _length) {
		int count = 0;
		Square current = this.root;
		for (; count < _length; count ++) {
			Square neighbor = current.neighbor(this.orientation, _sideWays);
			if (neighbor == null) {
				return count;
			}
			Square diagNeighbor = neighbor.neighbor(this.orientation, _verticle);
			if (diagNeighbor == null) {
				return count;
			}

			current = neighbor;
			this.add(diagNeighbor);
		}
		return count;
	}
	
	/**
	 * Adds up to [_length] number of squares in the given direction of this square.
	 * The number of squares that were added is returned. There may not be _length
	 * number of squares in this direction due to the edge of the board.
	 * @param length
	 * @return
	 */
	public int build(Direction _direction, int _length) {
		int count = 0;
		Square current = this.root;
		for (; count < _length; count ++) {
			Square neighbor = current.neighbor(this.orientation, _direction);
			if (neighbor == null) {
				return count;
			}
			
			current = neighbor;
			this.add(neighbor);
		}
		return count;
	}
	
	/**
	 * Does last second checks along the path assuming that no more squares will
	 * be added to the path. This mainly evaluates the ability for the piece along
	 * this path to jump over pieces.
	 * @param _canJump Determines whether or not the piece following this path may
	 * jump over pieces in the way.
	 */
	public boolean finish(boolean _canJump) {
		if (!_canJump) {
			for (int i = 0; i < this.size(); i ++) {
				Square square = this.get(i);
				if (this.isContinuous) {
					if (square.hasPiece()) {
						int startRemovingIndex = i;
						int origSize = this.size();
						if (square.getPiece().team != this.team)
							startRemovingIndex ++;
						for (; startRemovingIndex < origSize; startRemovingIndex ++) {
							this.removeLast();
						}
					}
				} else {
					if (!this.getLast().equals(square) && square.hasPiece())
						this.clear();
					else if (this.getLast().equals(square) && square.hasPiece(this.team))
						this.clear();
				}
			}
		}
		return this.size() > 0;
	}
	
	/**
	 * Determines whether or not this path includes a destination point that is the
	 * given _square. Note that just because this path might pass through this square
	 * does not always mean that the piece following this path can end on that square.
	 * For example, knights can only end their turn at the end of their path, not
	 * anywhere within.
	 * @param _square
	 * @return
	 */
	public boolean containsDestination(Square _square) {
		if (!this.isContinuous) {
			return this.getLast().equals(_square);
		}
		if (this.isContinuous) {
			return this.contains(_square);
		}
		return false;
	}
}
