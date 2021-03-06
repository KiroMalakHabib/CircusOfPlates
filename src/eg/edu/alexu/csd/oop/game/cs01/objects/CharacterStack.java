package eg.edu.alexu.csd.oop.game.cs01.objects;

import java.util.List;
import java.util.Stack;

import eg.edu.alexu.csd.oop.game.GameObject;
import eg.edu.alexu.csd.oop.game.cs01.DynamicLinkage.GameObjectLoader;
import eg.edu.alexu.csd.oop.game.cs01.Enums.ObjectType;
import eg.edu.alexu.csd.oop.game.cs01.Enums.Score;
import eg.edu.alexu.csd.oop.game.cs01.Logger4J.OurLogger;
import eg.edu.alexu.csd.oop.game.cs01.ObjectPool.FallenObjectsGenerator;
import eg.edu.alexu.csd.oop.game.cs01.SnapShot.CharacterStackSnapShot;
import eg.edu.alexu.csd.oop.game.cs01.SnapShot.FallenObjectSnapShot;
import eg.edu.alexu.csd.oop.game.cs01.Strategy.MovableXCondition;
import eg.edu.alexu.csd.oop.game.cs01.Strategy.NotMovableY;

public class CharacterStack extends AbstractGameObject {

	private final int MAX_FALLEN_OBJECTS = 15;
	private Stack<GameObject> stack;
	private List<GameObject> controlable;
	private ObjectType type;

	public CharacterStack() {
	}

	public CharacterStack(int x, int y, ObjectType type) {
		super(x, y, 35, 0);
		stack = new Stack<>();
		this.type = type;
	}

	public Score addFallenObject(GameObject fallenObject, List<GameObject> controlable) {
		this.controlable = controlable;
		if (CheckColors(fallenObject)) {
			FallenObjectsGenerator.getInstance().releaseObject(fallenObject);
			OurLogger.info(this.getClass(), "plate released into pool");
			removeFallenObject();
			return Score.win;
		}
		if (stack.size() < MAX_FALLEN_OBJECTS) {
			setY(getY() - 7);
			fallenObject.setX(getX());
			fallenObject.setY(getY());
			stack.push(fallenObject);
			((AbstractFallenObject) fallenObject).setMovableY(new NotMovableY(fallenObject.getY()));
			((AbstractFallenObject) fallenObject).setMovableX(new MovableXCondition(this));
			this.controlable.add(fallenObject);
			return Score.noChange;
		}
		return Score.lose;
	}

	/**
	 * the previous 2 plates were of the same color;
	 */
	private void removeFallenObject() {
		try {
			FallenObjectsGenerator.getInstance().releaseObject(stack.peek());
			OurLogger.info(this.getClass(), "plate released into pool");
			this.controlable.remove(stack.peek());
			stack.pop();
			FallenObjectsGenerator.getInstance().releaseObject(stack.peek());
			OurLogger.info(this.getClass(), "plate released into pool");
			this.controlable.remove(stack.peek());
			stack.pop();
			setY(getY() + 14);
		} catch (Exception e) {
			OurLogger.error(getClass(), e.getMessage());
		}
	}

	private boolean CheckColors(GameObject fallenObject) {
		try {
			if (((AbstractFallenObject) stack.get(stack.size() - 1)).getColor()
					.equals(((AbstractFallenObject) fallenObject).getColor())
					&& ((AbstractFallenObject) stack.get(stack.size() - 2)).getColor()
							.equals(((AbstractFallenObject) fallenObject).getColor())) {
				return true;
			}
		} catch (Exception e) {
			OurLogger.error(getClass(), e.getMessage());
		}
		return false;
	}

	public int getSize() {
		return stack.size();
	}

	public CharacterStackSnapShot getSnapShot() {
		return new CharacterStackSnapShot(this);
	}

	public void loadCharacterStack(CharacterStackSnapShot snapShot) {
		this.loadGameObject(snapShot);
		stack = new Stack<GameObject>();
		for (int i = 0; i < snapShot.getStack().size(); i++) {
			AbstractFallenObject o = GameObjectLoader.getInstance()
					.newInstance(((FallenObjectSnapShot) snapShot.getStack().get(i)).getClassName());
			o.loadFallenObject((FallenObjectSnapShot) snapShot.getStack().get(i));
			o.setMovableY(new NotMovableY(o.getY()));
			o.setMovableX(new MovableXCondition(this));
			stack.add(o);
		}
		this.type = snapShot.getType();
	}

	public ObjectType getType() {
		return this.type;
	}

	/**
	 * @return the stack
	 */
	public Stack<GameObject> getStack() {
		return stack;
	}

}
